def call(String logFilePath = "build.log") {
    try {
        def commitId = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
        def shortCommitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        def commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
        def commitAuthor = sh(script: 'git log -1 --pretty=format:%an', returnStdout: true).trim()
        def branchName = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
        def repoUrl = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
        def buildUser = currentBuild.getBuildCauses()[0]?.userName ?: 'GitHub Webhook or Unknown'

        // Generate git diff and save as attachment
        def gitDiff = sh(script: "git diff HEAD~1 HEAD", returnStdout: true).trim()
        writeFile file: "git_diff.txt", text: gitDiff

        // Email body
        def body = """
        <html>
        <body>
            <h2>🔔 Jenkins Build Notification</h2>
            <p><b>Job:</b> ${env.JOB_NAME}</p>
            <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
            <p><b>Triggered By:</b> ${buildUser}</p>
            <hr>
            <p><b>Commit ID:</b> ${commitId}</p>
            <p><b>Short ID:</b> ${shortCommitId}</p>
            <p><b>Author:</b> ${commitAuthor}</p>
            <p><b>Message:</b> ${commitMessage}</p>
            <p><b>Branch:</b> ${branchName}</p>
            <p><b>Repository:</b> <a href="${repoUrl}">${repoUrl}</a></p>
            <hr>
            <p>📎 Attachments:</p>
            <ul>
                <li><b>Build Log:</b> ${logFilePath}</li>
                <li><b>Git Diff:</b> git_diff.txt</li>
            </ul>
        </body>
        </html>
        """

        // Send the email
        emailext (
            subject: "Build #${env.BUILD_NUMBER} - ${env.JOB_NAME} [${shortCommitId}]",
            body: body,
            mimeType: 'text/html',
            to: "souravsingh8917@gmail.com,sourav.singh@shipease.in",
            attachmentsPattern: "${logFilePath},git_diff.txt"
        )
    } catch (err) {
        echo "Notification failed: ${err}"
    }
}
