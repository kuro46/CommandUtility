@file:JvmName("CompletionUtils")

package com.github.kuro46.commandutility

import org.bukkit.Bukkit

@JvmOverloads
fun <T> filterCandidates(
    candidates: Iterable<T>,
    filterBy: String,
    ignoreCase: Boolean = true,
    toStringFunc: (T) -> String = { it -> it.toString() }
): List<String> {
    return candidates
        .map { toStringFunc(it) }
        .filter { it.startsWith(filterBy, ignoreCase) }
}

