def call(Map config = [:]) {
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def buildStatus = currentBuild.currentResult ?: 'SUCCESS'
    def buildUrl = env.BUILD_URL
    def workspace = env.WORKSPACE
    def logFile = "${workspace}/build.log"
    def commitId = ''
    def commitMsg = ''
    def author = ''
    def repoUrl = ''
    def gitDiff = ''

    try {
        dir("${workspace}") {
            author = sh(script: "git log -1 --pretty=format:%an", returnStdout: true).trim()
            commitMsg = sh(script: "git log -1 --pretty=format:%s", returnStdout: true).trim()
            commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            repoUrl = sh(script: "git config --get remote.origin.url || echo 'N/A'", returnStdout: true).trim()
            gitDiff = sh(script: "git diff HEAD~1 HEAD || echo 'No diff available'", returnStdout: true).trim()
        }
    } catch (e) {
        echo "Notification failed: ${e}"
    }

    // Save build log
    try {
        sh "cp ${env.WORKSPACE}@log/../log build.log || echo 'No log found'"
    } catch (e) {
        echo "Log copy failed: ${e}"
    }

    emailext (
        subject: "${buildStatus}: ${jobName} #${buildNumber}",
        body: """
            <h2>Jenkins Build Notification</h2>
            <ul>
                <li><b>Job:</b> ${jobName}</li>
                <li><b>Build:</b> #${buildNumber}</li>
                <li><b>Status:</b> ${buildStatus}</li>
                <li><b>Commit ID:</b> ${commitId}</li>
                <li><b>Message:</b> ${commitMsg}</li>
                <li><b>Author:</b> ${author}</li>
                <li><b>Repo:</b> ${repoUrl}</li>
            </ul>
            <pre><code>${gitDiff}</code></pre>
            <p>View Build Logs: <a href="${buildUrl}">${buildUrl}</a></p>
        """,
        to: 'sourav.singh@shipease.in',
        mimeType: 'text/html',
        attachmentsPattern: 'build.log'
    )
}
