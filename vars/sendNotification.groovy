def call(env, List<String> additionalRecipients = []) {
    def commitAuthor = sh(script: "git log -1 --pretty=format:%an", returnStdout: true).trim()
    def commitMessage = sh(script: "git log -1 --pretty=format:%s", returnStdout: true).trim()
    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    def fullCommitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

    // Detect repo and format commit link
    def gitUrl = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
    def cleanedUrl = gitUrl
        .replaceFirst(/git@github.com:/, 'https://github.com/')
        .replaceFirst(/\.git$/, '')
    def commitUrl = "${cleanedUrl}/commit/${fullCommitId}"

    // Default recipient fallback
    def recipients = additionalRecipients.join(',') ?: "souravsingh8917@gmail.com"

    emailext(
        subject: "✅ Jenkins Build: ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """
            <h2>Build Report</h2>
            <ul>
              <li><b>Job:</b> ${env.JOB_NAME}</li>
              <li><b>Build Number:</b> ${env.BUILD_NUMBER}</li>
              <li><b>Status:</b> ${currentBuild.currentResult}</li>
              <li><b>Author:</b> ${commitAuthor}</li>
              <li><b>Commit:</b> ${commitMessage} (<code>${commitId}</code>)</li>
              <li><b>Commit Link:</b> <a href="${commitUrl}">${commitUrl}</a></li>
              <li><b>Build Link:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
            </ul>
        """,
        mimeType: 'text/html',
        to: recipients,
        attachLog: true
    )
}
