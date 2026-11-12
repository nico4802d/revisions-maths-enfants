package com.nico.revisionsmaths.core

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
    fun `addition pour 7 ans reste sous ou egale a 20`() {
        repeat(100) {
            val q = QuestionGenerator.generateQuestion(7, Operation.ADDITION)
            assertEquals(q.a + q.b, q.answer)
            assertTrue(q.answer <= 20)
        }
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
