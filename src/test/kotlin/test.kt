import java.io.File
import java.util.*

val slashData: DiscordSlashBuilder.() -> Unit = {
    chat {
        name = "cry"
        description = "Cries"

        subCommand {
            name = "hard"
            description = "Cries harder than usually"
            string {
                name = "receiver"
                description = "Who should be hugged?"
                choices = mutableListOf(Choice("Myself", "me"))
                required = true
            }

            int {
                name = "number"
                description = "Number of hugs"
                choices = mutableListOf(Choice("One", 1))
                required = true
            }
        }

        string {
            name = "receiver"
            description = "Who should be hugged?"
            choices = mutableListOf(Choice("Myself", "me"))
        }

        double {
            name = "price"
            description = "Hug price"
            choices = mutableListOf(Choice("None", 0.0))
        }
    }

    user {
        name = "cry with"
    }

    message {
        name = "cry about"
    }
}

fun main() {
    val config =
        Properties().let { props -> File("./src/test/kotlin/config").inputStream().use { props.load(it) }; props }

    val slashBuilder = DiscordSlashBuilder().apply(slashData)
    println(slashBuilder.prettyJson())

    println("Building now, syncing!")

    slashBuilder.id = config.getProperty("id")
    slashBuilder.token = config.getProperty("token")
    slashBuilder.public = config.getProperty("public")

    slashBuilder.sync()

    println("Syncing done")
}