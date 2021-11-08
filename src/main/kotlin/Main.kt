import io.ktor.application.*
import io.ktor.util.*

class DiscordSlash(config: Configuration) {
    val prop = config.public
    val path = config.path
    val builder = config.builder

    class Configuration {
        var public: String = ""
        var path: String = ""
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
    private var commands = mutableListOf<Command>()

    fun chat(init: Command.SlashCommand.() -> Unit) = commands.add(Command.SlashCommand().apply(init))
    fun message(init: Command.MessageCommand.() -> Unit) = commands.add(Command.MessageCommand().apply(init))
    fun user(init: Command.UserCommand.() -> Unit) = commands.add(Command.UserCommand().apply(init))

    sealed class Command {
        var name: String = ""
        var description: String = ""
        var defaultPermission: Boolean = false
        protected var type: Int = 0

        class SlashCommand : Command() {

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

            sealed class Option {
                protected var type: OptionType = OptionType.FILLER
                var name: String = ""
                var description: String = ""
                var required: Boolean = false
                var autocomplete: Boolean = false

                class SubCommand : Option() {
                    protected var options = mutableListOf<Option>()

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

                class GroupCommand : Option() {
                    protected var options = mutableListOf<Option>()

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

                class StringOption : Option() {
                    init {
                        type = OptionType.STRING
                    }

                    var choices = mutableMapOf<String, String>()
                }

                class IntOption : Option() {
                    init {
                        type = OptionType.INTEGER
                    }

                    var choices = mutableMapOf<String, Int>()

                    var min: Int? = null
                    var max: Int? = null
                }

                class DoubleOption : Option() {
                    init {
                        type = OptionType.NUMBER
                    }

                    var choices = mutableMapOf<String, Double>()
                    var min: Double? = null
                    var max: Double? = null
                }

                class BooleanOption : Option() {
                    init {
                        type = OptionType.BOOLEAN
                    }
                }

                class UserOption : Option() {
                    init {
                        type = OptionType.USER
                    }
                }

                class RoleOption : Option() {
                    init {
                        type = OptionType.ROLE
                    }
                }

                class MentionableOption : Option() {
                    init {
                        type = OptionType.MENTIONABLE
                    }
                }

                class ChannelOption : Option() {
                    init {
                        type = OptionType.CHANNEL
                    }

                    var channelTypes = mutableListOf<ChannelType>()
                }
            }
        }

        class UserCommand : Command() {
            init {
                type = 2
            }
        }

        class MessageCommand : Command() {
            init {
                type = 3
            }
        }
    }
}

enum class OptionType {
    FILLER,

    SUB_COMMAND, SUB_COMMAND_GROUP,

    STRING, INTEGER, BOOLEAN,

    USER, CHANNEL, ROLE, MENTIONABLE,

    NUMBER
}

enum class ChannelType {
    GUILD_TEXT, DM,
    GUILD_VOICE, GROUP_DM,
    GUILD_CATEGORY,

    GUILD_NEWS, GUILD_STORE,

    GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD,

    GUILD_STAGE_VOICE,
}