package org.pettersson.location

import kotlin.math.*

object LocationFormatter {

    /**
     * format value as a degree string
     * e.g. 12.5 -> 12°30'
     *
     * precision <= 0: only include full degrees
     *
     * precision = 1:  include degrees and minutes
     *
     * precision >= 2: include degrees, minutes and seconds with precision-2 decimal places
     */
    fun formatDMS(value : Double, precision : Int) : String {

        val prefix = if (value < 0) "-" else ""

        var tmp = abs(value)

        if(precision <= 0)
            return "$prefix${round(tmp)}°"

        val degrees = truncate(tmp).toInt()

        tmp = (tmp - degrees) * 60

        if(precision == 1)
            return "$prefix$degrees°${round(tmp)}'"

        val minutes = truncate(tmp).toInt()
        val seconds = (tmp - minutes) * 60

        return "$prefix$degrees°$minutes'" + "%.${precision - 2}f".format(seconds) + "\""
    }

    /**
     * format value as a latitude string
     * e.g. 12.5 -> 12°30' N
     *
     * precision <= 0: only include full degrees
     *
     * precision = 1:  include degrees and minutes
     *
     * precision >= 2: include degrees, minutes and seconds with precision-2 decimal places
     */
    fun latitudeAsDMS(latitude: Double, precision: Int): String {
        return formatDMS(abs(latitude), precision) + if (latitude >= 0) "N" else "S"
    }

    /**
     * format value as a longitude string
     * e.g. 12.5 -> 12°30' E
     *
     * precision <= 0: only include full degrees
     *
     * precision = 1:  include degrees and minutes
     *
     * precision >= 2: include degrees, minutes and seconds with precision-2 decimal places
     */
    fun longitudeAsDMS(longitude: Double, precision: Int): String {
        return formatDMS(abs(longitude), precision) + if (longitude >= 0) "E" else "W"
    }
}