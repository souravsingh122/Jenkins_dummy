def call(env) {
    echo "==== Starting sendNotification ===="
    try {
        emailext(
            subject: "Build Result: ${env.JOB_NAME} - #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
            body: """
                <p><b>Job:</b> ${env.JOB_NAME}</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Status:</b> ${currentBuild.currentResult}</p>
                <p><b>URL:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
            """,
            mimeType: 'text/html',
            to: 'souravsingh8917@gmail.com'
        )
        echo "==== Email sent successfully ===="
    } catch (e) {
        echo "==== Failed to send email ===="
        echo "Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
    }
}
