import io.ktor.application.*
import io.ktor.util.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonArray
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset

class DiscordSlash(config: Configuration) {
    val builder = config.builder
    val path = config.path

    private val instance = DiscordSlashBuilder().apply(builder)

    class Configuration {
        var path = "/"
        var builder: DiscordSlashBuilder.() -> Unit = {}
    }

    // Implements ApplicationFeature as a companion object.
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, DiscordSlash> {
        // Creates a unique key for the feature.
        override val key = AttributeKey<DiscordSlash>("DiscordSlash")

        // Code to execute when installing the plugin.
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): DiscordSlash {

            // It is responsibility of the install code to call the `configure` method with the mutable configuration.
            val configuration = Configuration().apply(configure)

            // Create the plugin, providing the mutable configuration so the plugin reads it keeping an immutable copy of the properties.
            val feature = DiscordSlash(configuration)

            feature.instance.sync()

            // Intercept a pipeline.
            pipeline.intercept(ApplicationCallPipeline.Call) {
                // Perform things in that interception point.
            }
            return feature
        }
    }
}

@DslMarker
annotation class SlashMarker

@SlashMarker
class DiscordSlashBuilder {
    var commands = mutableListOf<Command>()
    private val basePath = "https://discord.com/api/v9"
    private val instance = HttpClient.newHttpClient()

    var public: String = ""
    var id: String = ""
    var token: String = ""

    @OptIn(ExperimentalSerializationApi::class)
    fun sync() {
        val url = URL("$basePath/applications/${id}/commands")

        val je = url.openConnection().let {
            it.setRequestProperty("Authorization", "Bot $token")
            Json.decodeFromStream<JsonElement>(it.getInputStream())
        }

        if (je.jsonArray.size != commands.size) {
            val req = HttpRequest.newBuilder(URI("$basePath/applications/${id}/commands")).apply {
                header("Authorization", "Bot $token")
                header("Content-Type", "application/json")
                PUT(HttpRequest.BodyPublishers.ofString(json()))
            }.build()

            instance.send(req, HttpResponse.BodyHandlers.ofString()).let {
                if (it.statusCode() != 200) {
                    throw Error("Something went wrong with request expected status 200, got ${it.statusCode()} \nBody:\n${it.body()}")
                }
            }
        }
    }

    fun json(): String {
        @Suppress("JSON_FORMAT_REDUNDANT")
        val json = Json { classDiscriminator = "#class" }
        return json.encodeToString(commands)
    }

    fun prettyJson(): String {
        @Suppress("JSON_FORMAT_REDUNDANT")
        val json = Json { classDiscriminator = "#class"; prettyPrint = true }
        return json.encodeToString(commands)
    }

    fun chat(init: Command.SlashCommand.() -> Unit) = commands.add(Command.SlashCommand().apply(init))
    fun message(init: Command.MessageCommand.() -> Unit) = commands.add(Command.MessageCommand().apply(init))
    fun user(init: Command.UserCommand.() -> Unit) = commands.add(Command.UserCommand().apply(init))
}

@Serializable
sealed class Command {
    var name: String = ""
    var defaultPermission: Boolean = false
    var type: Int = 0

    @Serializable
    class SlashCommand : Command() {

        var description: String = ""

        init {
            type = 1
        }

        private var options = mutableListOf<Option>()

        fun subCommand(init: Option.SubCommand.() -> Unit) = options.add(Option.SubCommand().apply(init))
        fun group(init: Option.GroupCommand.() -> Unit) = options.add(Option.GroupCommand().apply(init))
        fun string(init: Option.StringOption.() -> Unit) = options.add(Option.StringOption().apply(init))
        fun int(init: Option.IntOption.() -> Unit) = options.add(Option.IntOption().apply(init))
        fun double(init: Option.DoubleOption.() -> Unit) = options.add(Option.DoubleOption().apply(init))
        fun boolean(init: Option.BooleanOption.() -> Unit) = options.add(Option.BooleanOption().apply(init))
        fun user(init: Option.UserOption.() -> Unit) = options.add(Option.UserOption().apply(init))
        fun channel(init: Option.ChannelOption.() -> Unit) = options.add(Option.ChannelOption().apply(init))
        fun role(init: Option.RoleOption.() -> Unit) = options.add(Option.RoleOption().apply(init))
        fun mentionable(init: Option.MentionableOption.() -> Unit) =
            options.add(Option.MentionableOption().apply(init))


        @Serializable
        sealed class Option {
            var type: OptionType = OptionType.FILLER
            var name: String = ""
            var description: String = ""
            var required: Boolean = false
            var autocomplete: Boolean = false


            @Serializable
            class SubCommand : Option() {
                var options = mutableListOf<Option>()

                init {
                    type = OptionType.SUB_COMMAND
                }

                fun string(init: StringOption.() -> Unit) = options.add(StringOption().apply(init))
                fun int(init: IntOption.() -> Unit) = options.add(IntOption().apply(init))
                fun double(init: DoubleOption.() -> Unit) = options.add(DoubleOption().apply(init))
                fun boolean(init: BooleanOption.() -> Unit) = options.add(BooleanOption().apply(init))
                fun user(init: UserOption.() -> Unit) = options.add(UserOption().apply(init))
                fun channel(init: ChannelOption.() -> Unit) = options.add(ChannelOption().apply(init))
                fun role(init: RoleOption.() -> Unit) = options.add(RoleOption().apply(init))
                fun mentionable(init: MentionableOption.() -> Unit) = options.add(MentionableOption().apply(init))
            }


            @Serializable
            class GroupCommand : Option() {
                var options = mutableListOf<Option>()

                init {
                    type = OptionType.SUB_COMMAND_GROUP
                }

                fun subCommand(init: SubCommand.() -> Unit) = options.add(SubCommand().apply(init))
                fun string(init: StringOption.() -> Unit) = options.add(StringOption().apply(init))
                fun int(init: IntOption.() -> Unit) = options.add(IntOption().apply(init))
                fun double(init: DoubleOption.() -> Unit) = options.add(DoubleOption().apply(init))
                fun boolean(init: BooleanOption.() -> Unit) = options.add(BooleanOption().apply(init))
                fun user(init: UserOption.() -> Unit) = options.add(UserOption().apply(init))
                fun channel(init: ChannelOption.() -> Unit) = options.add(ChannelOption().apply(init))
                fun role(init: RoleOption.() -> Unit) = options.add(RoleOption().apply(init))
                fun mentionable(init: MentionableOption.() -> Unit) = options.add(MentionableOption().apply(init))
            }

            @Serializable
            class StringOption : Option() {
                init {
                    type = OptionType.STRING
                }

                var choices = mutableListOf<Choice<String>>()
            }

            @Serializable
            class IntOption : Option() {
                init {
                    type = OptionType.INTEGER
                }

                var choices = mutableListOf<Choice<Int>>()

                var min: Int? = null
                var max: Int? = null
            }


            @Serializable
            class DoubleOption : Option() {
                init {
                    type = OptionType.NUMBER
                }

                var choices = mutableListOf<Choice<Double>>()
                var min: Double? = null
                var max: Double? = null
            }


            @Serializable
            class BooleanOption : Option() {
                init {
                    type = OptionType.BOOLEAN
                }
            }


            @Serializable
            class UserOption : Option() {
                init {
                    type = OptionType.USER
                }
            }


            @Serializable
            class RoleOption : Option() {
                init {
                    type = OptionType.ROLE
                }
            }


            @Serializable
            class MentionableOption : Option() {
                init {
                    type = OptionType.MENTIONABLE
                }
            }


            @Serializable
            class ChannelOption : Option() {
                init {
                    type = OptionType.CHANNEL
                }

                var channelTypes = mutableListOf<ChannelType>()
            }
        }
    }

    @Serializable
    class UserCommand : Command() {
        init {
            type = 2
        }
    }

    @Serializable
    class MessageCommand : Command() {
        init {
            type = 3
        }
    }
}

@Serializable(with = TypeSerializer::class)
enum class OptionType {
    FILLER,

    SUB_COMMAND, SUB_COMMAND_GROUP,

    STRING, INTEGER, BOOLEAN,

    USER, CHANNEL, ROLE, MENTIONABLE,

    NUMBER
}

@Serializable(with = ChannelTypeSerializer::class)
enum class ChannelType {
    GUILD_TEXT, DM,
    GUILD_VOICE, GROUP_DM,
    GUILD_CATEGORY,

    GUILD_NEWS, GUILD_STORE,

    GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD,

    GUILD_STAGE_VOICE,
}

object TypeSerializer : KSerializer<OptionType> {
    override fun deserialize(decoder: Decoder): OptionType {
        val data = decoder.decodeInt()
        return OptionType.values().find { it.ordinal == data } ?: OptionType.FILLER
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: OptionType) {
        encoder.encodeInt(value.ordinal)
    }

}

object ChannelTypeSerializer : KSerializer<ChannelType> {
    override fun deserialize(decoder: Decoder): ChannelType {
        val data = decoder.decodeInt()
        return ChannelType.values().find { it.ordinal == data } ?: ChannelType.GUILD_TEXT
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ChannelType) {
        encoder.encodeInt(value.ordinal)
    }
}

@Serializable
data class Choice<T>(val name: String, val value: T)