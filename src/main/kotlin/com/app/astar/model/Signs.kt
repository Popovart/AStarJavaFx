package com.app.astar.model

import javafx.scene.paint.Color
import javafx.scene.paint.Paint

enum class Signs(val char: Char) {
    WALL('#'),
    UNVISITED('.'),
    EXIT('E'),
    START('S'),
    PATH('*');

    override fun toString() = char.toString();
}

enum class ColorSigns(val color: Color) {
    WALL(Color.rgb(0,0,0)),
    UNVISITED(Color.rgb(255,255,255)),
    EXIT(Color.rgb(255, 87, 51)),
    START(Color.rgb(218, 247, 166)),
    PATH(Color.rgb(0,255,255));
}

