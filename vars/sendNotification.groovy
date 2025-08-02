def call(env) {
    def commitId = ''
    def commitMsg = ''
    def authorName = ''
    def remoteUrl = ''
    def diffFile = 'git_diff.txt'
    def logFile = 'build_log.txt'
    def githubCommitUrl = ''
    def githubDiffUrl = ''
    def recipients = 'souravsingh8917@gmail.com sourav.singh@shipease.in'

    try {
        commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
        commitMsg = sh(script: "git log -1 --pretty=%s", returnStdout: true).trim()
        authorName = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
        remoteUrl = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()

        // Convert SSH or HTTPS URL to GitHub URL
        if (remoteUrl.contains("git@github.com")) {
            remoteUrl = remoteUrl.replace("git@github.com:", "https://github.com/").replace(".git", "")
        } else if (remoteUrl.contains("https://github.com")) {
            remoteUrl = remoteUrl.replace(".git", "")
        }

        githubCommitUrl = "${remoteUrl}/commit/${commitId}"
        githubDiffUrl = "${remoteUrl}/compare/${commitId}~1...${commitId}"

        // Save full git diff to file
        sh(script: "git show ${commitId} > ${diffFile}")
    } catch (Exception e) {
        echo "Warning: Git info unavailable - ${e.message}"
        commitId = 'N/A'
        commitMsg = 'N/A'
        authorName = 'N/A'
        githubCommitUrl = '#'
        githubDiffUrl = '#'
        writeFile file: diffFile, text: 'Git diff not available.'
    }

    // Save Jenkins build log to file
    writeFile file: logFile, text: currentBuild.rawBuild.getLog().join('\n')

    emailext(
        to: recipients,
        subject: "🔔 Jenkins Job ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """
        <html>
        <body>
            <h2>🔔 Jenkins Build Notification</h2>
            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
            <p><strong>Build #:</strong> ${env.BUILD_NUMBER}</p>
            <p><strong>Status:</strong> ${currentBuild.currentResult}</p>
            <p><strong>Author:</strong> ${authorName}</p>
            <p><strong>Commit:</strong> <a href="${githubCommitUrl}">${commitId}</a></p>
            <p><strong>Message:</strong> ${commitMsg}</p>
            <p><strong>Diff:</strong> <a href="${githubDiffUrl}">View Code Changes</a></p>
            <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
            <br/>
            <p><strong>Attachments:</strong></p>
            <ul>
                <li><code>git_diff.txt</code> – Git changes</li>
                <li><code>build_log.txt</code> – Console output</li>
            </ul>
        </body>
        </html>
        """,
        mimeType: 'text/html',
        attachmentsPattern: "${diffFile}, ${logFile}"
    )
}
