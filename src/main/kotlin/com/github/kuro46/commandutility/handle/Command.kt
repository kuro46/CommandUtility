package com.github.kuro46.commandutility.handle

/**
 * CommandUtility version of [org.bukkit.command.Command]
 *
 * @property handler Handler of this command.
 * @property description Description of this command. Default is `null`.
 */
class Command(
    val sections: CommandSections,
    val handler: CommandHandler,
    val description: String? = null
) {

    /**
     * Checks that is this equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Command) {
            return false
        }
        return this.sections == other.sections
    }

    /**
     * Returns hash of this.
     */
    override fun hashCode(): Int {
        return sections.hashCode()
    }

    /**
     * Returns a string representation of this instance.
     */
    override fun toString(): String {
        return sections.toString()
    }
}
