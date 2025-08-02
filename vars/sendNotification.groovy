def call(env) {
    def gitAuthor = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
    def gitMsg = sh(script: "git log -1 --pretty=format:'%s'", returnStdout: true).trim()
    def gitCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

    emailext(
        subject: "[${env.JOB_NAME}] Build #${env.BUILD_NUMBER} - ${env.BUILD_STATUS ?: 'Completed'}",
        body: """<h3>Build Notification</h3>
<p><b>Job:</b> ${env.JOB_NAME}</p>
<p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
<p><b>Status:</b> ${env.BUILD_STATUS ?: 'SUCCESS'}</p>
<p><b>Author:</b> ${gitAuthor}</p>
<p><b>Commit:</b> ${gitCommit}</p>
<p><b>Message:</b> ${gitMsg}</p>
<p><a href='${env.BUILD_URL}console'>View Build Logs</a></p>
""", 
        to: 'your-team@example.com',
        mimeType: 'text/html'
    )
}