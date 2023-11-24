package com.app.astar.model

import kotlin.math.abs

class Point (val col: Int, val row: Int) : Comparable<Point> {
    fun getDistOfPoints(other: Point): Int {
        return abs(col - other.col) + abs(row - other.row)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        return col == other.col && row == other.row
    }

    override fun hashCode(): Int {
        var result = col
        result = 31 * result + row
        return result
    }

    override fun compareTo(other: Point): Int {
        if (this.col == other.col && this.row == other.row) {
            return 0
        }
        return when {
            col > other.col -> 1
            col < other.col -> -1
            else -> row.compareTo(other.row)
        }
    }

    override fun toString(): String {
        return "($col, $row)"
    }


}