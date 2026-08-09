# Heroku demo deployment

The repository is configured for Heroku's Gradle buildpack and produces an executable Spring Boot WAR so JSP views remain available.

1. Create or select a Heroku app.
2. Connect the app to this GitHub repository, or run `heroku git:remote -a <app-name>`.
3. Deploy `master`. Heroku runs the Gradle `stage` task and starts the command in `Procfile`.

The application reads Heroku's assigned `PORT` through the `Procfile`. No fixed server port is required.
