fun main() {
    val slashBuilder = DiscordSlashBuilder()

    slashBuilder.chat {
        name = "cry"
        description = "Cries"

        subCommand {
            name = "hard"
            description = "Cries harder than usually"
            string {
                name = "receiver"
                description = "Who should be hugged?"
                choices = mutableMapOf("Myself" to "me")
                required = true
            }

            int {
                name = "Number"
                description = "Number of hugs"
                choices = mutableMapOf("One" to 1)
                required = true
            }
        }

        string {
            name = "receiver"
            description = "Who should be hugged?"
            choices = mutableMapOf("Myself" to "me")
        }

        double {
            name = "price"
            description = "Hug price"
            choices = mutableMapOf("None" to 0.0)
        }
    }

    slashBuilder.user {
        name = "cry with"
        description = "Cries with user"
    }

    slashBuilder.message {
        name = "cry about"
        description = "Cries about message"
        defaultPermission = false
    }
}