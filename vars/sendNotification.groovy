def call(env) {
    try {
        def subject = sh(script: 'git log -1 --pretty=format:%s', returnStdout: true).trim()
        def shortCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        def fullCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
        def branch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()

        // Dynamically fetch correct remote URL from any remote (origin, sourav, etc.)
        def gitUrl = sh(
            script: '''git remote -v | grep fetch | head -n1 | awk '{print $2}' ''',
            returnStdout: true
        ).trim()

        // Transform Git URL (support both HTTPS and SSH)
        if (gitUrl.startsWith("git@")) {
            gitUrl = gitUrl.replace(":", "/").replaceFirst("git@", "https://")
        }
        gitUrl = gitUrl.replace(".git", "")

        def commitLink = "${gitUrl}/commit/${fullCommit}"
        def branchLink = "${gitUrl}/tree/${branch}"

        emailext(
            subject: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' - ${currentBuild.currentResult}",
            body: """
                <p><b>Job:</b> ${env.JOB_NAME}</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Build Status:</b> ${currentBuild.currentResult}</p>
                <p><b>Branch:</b> <a href="${branchLink}">${branch}</a></p>
                <p><b>Commit:</b> ${shortCommit}</p>
                <p><b>Commit Message:</b> ${subject}</p>
                <p><b>Commit Link:</b> <a href="${commitLink}">${commitLink}</a></p>
                <p><b>Build Logs:</b> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
            """,
            mimeType: 'text/html',
            to: "${env.EMAIL_RECIPIENTS ?: 'souravsingh8917@gmail.com'}"
        )
    } catch (Exception e) {
        echo "Notification failed: ${e.getMessage()}"
    }
}
