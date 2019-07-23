package com.github.kuro46.commandutility.handle

/**
 * A section of command.
 *
 * `/section1 section2 section3 section4...`
 *
 * @param name Name of this section.
 */
class CommandSection(name: String) : Comparable<CommandSection> {

    /**
     * Name of this section.
     */
    val name = name.toLowerCase()

    /**
     * Checks that is this equal to [other].
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is CommandSection) {
            return false
        }
        return this.name == other.name
    }

    /**
     * Returns hash of this.
     */
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

    /**
     * Compare to [other].
     */
    override fun compareTo(other: CommandSection): Int {
        return this.name.compareTo(other.name)
    }
}
