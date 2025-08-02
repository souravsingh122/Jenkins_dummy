def call(env, List<String> additionalRecipients = []) {
    def commitAuthor = sh(script: "git log -1 --pretty=format:%an", returnStdout: true).trim()
    def commitMessage = sh(script: "git log -1 --pretty=format:%s", returnStdout: true).trim()
    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    def fullCommitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

    def gitUrl = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
    def cleanedUrl = gitUrl.replaceAll(/git@github.com:/, 'https://github.com/').replaceAll(/.git$/, '')
    def commitUrl = "${cleanedUrl}/commit/${fullCommitId}"

    def emailParams = [
        subject: "✅ Jenkins Build: ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """
            <p><b>Job:</b> ${env.JOB_NAME}</p>
            <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
            <p><b>Status:</b> ${currentBuild.currentResult}</p>
            <p><b>Author:</b> ${commitAuthor}</p>
            <p><b>Commit:</b> ${commitMessage} (<code>${commitId}</code>)</p>
            <p><b>View Commit:</b> <a href="${commitUrl}">${commitUrl}</a></p>
            <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
        """,
        mimeType: 'text/html',
        attachLog: true
    ]

    if (additionalRecipients && !additionalRecipients.isEmpty()) {
        emailParams.to = additionalRecipients.join(',')
    }

    emailext(emailParams)
}
