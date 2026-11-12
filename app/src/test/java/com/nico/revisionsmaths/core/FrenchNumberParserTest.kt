package com.nico.revisionsmaths.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrenchNumberParserTest {

    @Test
    fun `chiffres arabes`() {
        assertEquals(56, FrenchNumberParser.parse("56"))
        assertEquals(0, FrenchNumberParser.parse("0"))
        assertEquals(100, FrenchNumberParser.parse("100"))
    }

    @Test
    fun `nombres en lettres simples`() {
        assertEquals(5, FrenchNumberParser.parse("cinq"))
        assertEquals(16, FrenchNumberParser.parse("seize"))
        assertEquals(17, FrenchNumberParser.parse("dix-sept"))
        assertEquals(21, FrenchNumberParser.parse("vingt et un"))
        assertEquals(42, FrenchNumberParser.parse("quarante-deux"))
    }

    @Test
    fun `nombres en lettres avec pieges francais`() {
        assertEquals(56, FrenchNumberParser.parse("cinquante-six"))
        assertEquals(70, FrenchNumberParser.parse("soixante-dix"))
        assertEquals(72, FrenchNumberParser.parse("soixante-douze"))
        assertEquals(80, FrenchNumberParser.parse("quatre-vingts"))
        assertEquals(91, FrenchNumberParser.parse("quatre-vingt-onze"))
        assertEquals(99, FrenchNumberParser.parse("quatre-vingt-dix-neuf"))
        assertEquals(104, FrenchNumberParser.parse("cent quatre"))
        assertEquals(200, FrenchNumberParser.parse("deux cents"))
    }

    @Test
    fun `phrase complete avec mots parasites`() {
        assertEquals(48, FrenchNumberParser.parse("la réponse est quarante-huit"))
        assertEquals(63, FrenchNumberParser.parse("je pense que c'est soixante-trois"))
    }

    @Test
    fun `entree vide ou inconnue`() {
        assertNull(FrenchNumberParser.parse(""))
        assertNull(FrenchNumberParser.parse("je ne sais pas"))
    }
}
