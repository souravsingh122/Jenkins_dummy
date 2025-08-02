def call(env) {
    def commitId = ''
    def commitMsg = ''
    def diffFile = 'git_diff.txt'
    def logFile = 'build_log.txt'
    def recipients = 'souravsingh8917@gmail.com sourav.singh@shipease.in'

    try {
        commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
        commitMsg = sh(script: "git log -1 --pretty=format:'%s'", returnStdout: true).trim()
        sh(script: "git show ${commitId} > ${diffFile}")
    } catch (Exception e) {
        echo "Warning: Git commit info could not be retrieved. ${e}"
        commitId = 'N/A'
        commitMsg = 'N/A'
        writeFile file: diffFile, text: 'Git diff not available due to error.'
    }

    // Save Jenkins build logs
    writeFile file: logFile, text: currentBuild.rawBuild.getLog().join('\n')

    emailext(
        to: recipients,
        subject: "Jenkins Job - ${env.JOB_NAME} Build #${env.BUILD_NUMBER} Status: ${currentBuild.currentResult}",
        body: """
<html>
<body>
    <p><b>Job Name:</b> ${env.JOB_NAME}</p>
    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
    <p><b>Status:</b> ${currentBuild.currentResult}</p>
    <p><b>Git Commit ID:</b> ${commitId}</p>
    <p><b>Git Commit Message:</b> ${commitMsg}</p>
    <p>See attached files for:</p>
    <ul>
        <li>💾 Full build logs</li>
        <li>🔍 Git diff of latest commit</li>
    </ul>
</body>
</html>
""",
        mimeType: 'text/html',
        attachmentsPattern: "${logFile}, ${diffFile}"
    )
}
