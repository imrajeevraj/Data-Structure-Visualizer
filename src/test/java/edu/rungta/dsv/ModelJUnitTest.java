package edu.rungta.dsv;
import org.junit.jupiter.api.Test;
public class ModelJUnitTest {
    @Test void linkedList(){Verification.linkedList();}
    @Test void stackQueue(){Verification.stackQueue();}
    @Test void bst(){Verification.bst();}
    @Test void rotations(){Verification.rotations();}
    @Test void avlDeletion(){Verification.avlDeletion();}
    @Test void randomizedTrees(){Verification.randomTrees();}
    @Test void randomizedLinear(){Verification.randomLinear();}
    @Test void boundaries(){Verification.limitsAndValidation();}
    @Test void immutableSnapshots(){Verification.immutableTraces();}
    @Test void demos(){Verification.controllerDemos();}
    @Test void playback()throws Exception{Verification.playback();}
    @Test void swing()throws Exception{Verification.interfaceChecks();}
}
