package com.nico.revisionsmaths.core

import com.nico.revisionsmaths.data.model.FixedTable
import com.nico.revisionsmaths.data.model.Operation
import com.nico.revisionsmaths.data.model.OperationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionGeneratorTest {

    @Test
    fun `addition pour 6 ans reste sous ou egale a 10`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(6, Operation.ADDITION)
            assertEquals(q.a + q.b, q.answer)
            assertTrue("a=${q.a}", q.a in 0..10)
            assertTrue("b=${q.b}", q.b in 0..10)
            assertTrue("reponse=${q.answer}", q.answer <= 10)
        }
    }

    @Test
    fun `multiplication pour 6 ans utilise les tables 1 a 3`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(6, Operation.MULTIPLICATION)
            assertEquals(q.a * q.b, q.answer)
            assertTrue(q.a in 1..3)
            assertTrue(q.b in 1..3)
        }
    }

    @Test
    fun `addition pour 7 ans reste sous ou egale a 20 sans operande nul`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(7, Operation.ADDITION)
            assertEquals(q.a + q.b, q.answer)
            assertTrue(q.answer <= 20)
            assertTrue("a=${q.a}", q.a >= 1)
            assertTrue("b=${q.b}", q.b >= 1)
        }
    }

    @Test
    fun `table fixe de multiplication des 8 reste dans la table des 8`() {
        val fixed = FixedTable(Operation.MULTIPLICATION, 8)
        repeat(50) {
            val q = QuestionGenerator.questionForTable(fixed)
            assertEquals(8, q.a)
            assertTrue(q.b in 1..10)
            assertEquals(q.a * q.b, q.answer)
        }
        val questions = QuestionGenerator.generateForTable(fixed, 15)
        assertEquals(15, questions.size)
        assertTrue(questions.all { it.a == 8 && it.op == Operation.MULTIPLICATION })
    }

    @Test
    fun `table fixe d addition des 7 reste dans la table des 7`() {
        val fixed = FixedTable(Operation.ADDITION, 7)
        repeat(50) {
            val q = QuestionGenerator.questionForTable(fixed)
            assertEquals(7, q.a)
            assertTrue(q.b in 1..10)
            assertEquals(q.a + q.b, q.answer)
        }
    }

    @Test
    fun `encodage et decodage de table fixe`() {
        val fixed = FixedTable(Operation.MULTIPLICATION, 8)
        assertEquals("M8", fixed.encode())
        assertEquals(fixed, FixedTable.decode("M8"))
        assertEquals(FixedTable(Operation.ADDITION, 3), FixedTable.decode("A3"))
        assertEquals(null, FixedTable.decode("-"))
        assertEquals(null, FixedTable.decode(null))
        assertEquals(null, FixedTable.decode("M99"))
    }

    @Test
    fun `multiplication pour 8 ans utilise les tables 2 a 9`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(8, Operation.MULTIPLICATION)
            assertEquals(q.a * q.b, q.answer)
            assertTrue(q.a in 2..9)
            assertTrue(q.b in 2..9)
        }
    }

    @Test
    fun `addition pour 9 ans et plus a deux chiffres`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(10, Operation.ADDITION)
            assertEquals(q.a + q.b, q.answer)
            assertTrue(q.a in 10..89)
            assertTrue(q.b in 10..89)
        }
    }

    @Test
    fun `multiplication pour 9 ans et plus utilise les tables 2 a 10`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(9, Operation.MULTIPLICATION)
            assertEquals(q.a * q.b, q.answer)
            assertTrue(q.a in 2..10)
            assertTrue(q.b in 2..10)
        }
    }

    @Test
    fun `mode mixte genere les deux types d'operations`() {
        val questions = QuestionGenerator.generate(8, OperationType.MIXED, 60)
        assertEquals(60, questions.size)
        assertTrue(questions.any { it.op == Operation.ADDITION })
        assertTrue(questions.any { it.op == Operation.MULTIPLICATION })
    }

    @Test
    fun `le nombre de questions generees est exact`() {
        assertEquals(5, QuestionGenerator.generate(7, OperationType.ADDITION, 5).size)
        assertEquals(20, QuestionGenerator.generate(7, OperationType.MULTIPLICATION, 20).size)
    }
}
