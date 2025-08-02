def call(env) {
    try {
        // Capture Git commit information
        def commitAuthor = sh(script: "git log -1 --pretty=format:%an", returnStdout: true).trim()
        def commitMessage = sh(script: "git log -1 --pretty=format:%s", returnStdout: true).trim()
        def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        def fullCommitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
        def repoUrl = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
        
        // Get git diff for the last commit only
        def gitDiff = sh(script: "git diff HEAD~1 HEAD", returnStdout: true).trim()

        // Save full build logs
        def logFilePath = "${env.WORKSPACE}/build.log"
        writeFile file: logFilePath, text: currentBuild.rawBuild.getLog().join("\n")

        // Email subject
        def subject = "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}"

        // Email body as HTML
        def body = """
            <h2>Jenkins Build Notification</h2>
            <ul>
                <li><strong>Job:</strong> ${env.JOB_NAME}</li>
                <li><strong>Build Number:</strong> #${env.BUILD_NUMBER}</li>
                <li><strong>Status:</strong> <b>${currentBuild.currentResult}</b></li>
                <li><strong>Commit:</strong> <code>${commitId}</code></li>
                <li><strong>Author:</strong> ${commitAuthor}</li>
                <li><strong>Message:</strong> ${commitMessage}</li>
                <li><strong>Repository:</strong> ${repoUrl}</li>
            </ul>
            <h3>Code Changes:</h3>
            <pre style="background-color:#f4f4f4;padding:10px;border:1px solid #ccc;white-space:pre-wrap;">${gitDiff}</pre>
            <hr>
            <p>Attached is the full build log for details.</p>
        """

        // Send the email with log file
        emailext(
            subject: subject,
            body: body,
            mimeType: 'text/html',
            to: 'sourav.singh@shipease.in',
            attachmentsPattern: 'build.log'
        )
    } catch (err) {
        echo "Notification failed: ${err.getMessage()}"
    }
}
