package com.github.kuro46.commandutility.handle

import java.util.Objects

/**
 * A class for holds CommandSection.
 *
 * ```
 * /section1 section2 section3 section4...
 * \---------------sections--------------/
 * ```
 *
 * @property sections List of sections.
 */
data class CommandSections(
    val sections: List<CommandSection>
) : List<CommandSection> by sections {

    override fun equals(other: Any?): Boolean {
        if (
            other == null ||
            other !is CommandSections ||
            this.size != other.size
        ) {
            return false
        }
        for (index in 0..this.lastIndex) {
            if (this.get(index) != other.get(index)) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(sections)
    }

    override fun toString(): String {
        return sections.joinToString(" ")
    }

    companion object {

        fun fromStrings(strings: Iterable<String>): CommandSections {
            return CommandSections(strings.map { CommandSection(it) })
        }
    }
}
