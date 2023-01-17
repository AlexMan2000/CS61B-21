package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE


    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> alist = new AListNoResizing<>();
        BuggyAList<Integer> blist = new BuggyAList<>();


        // Add for both
        for(int i=0; i<100;i++){
            alist.addLast(i);
            blist.addLast(i);
        }


        // remove for both
        assertEquals(alist.removeLast(), blist.removeLast());
        assertEquals(alist.removeLast(), blist.removeLast());
    }


    @Test
    public void testThreeAddThreeRemove2() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        correct.addLast(5);
        correct.addLast(10);
        correct.addLast(15);

        broken.addLast(5);
        broken.addLast(10);
        broken.addLast(15);

        assertEquals(correct.size(), broken.size());

        assertEquals(correct.removeLast(), broken.removeLast());
        assertEquals(correct.removeLast(), broken.removeLast());
        assertEquals(correct.removeLast(), broken.removeLast());
    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
                System.out.println("addLast A(" + randVal + ")");
                System.out.println("addLast B(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeA = L.size();
                int sizeB = B.size();
                System.out.println("sizeA: " + sizeA);
                System.out.println("sizeB: " + sizeB);
                assertEquals(sizeA, sizeB);
            }  else if (operationNumber == 2 && L.size() > 0) {
                // last
                int lastA = L.removeLast();
                int lastB = B.removeLast();
                System.out.println("removelastA: " + lastA);
                System.out.println("removelastB: " + lastB);
                assertEquals(lastA, lastB);
            } else if (operationNumber == 3 && L.size() > 0) {
                // last
                int lastA = L.getLast();
                int lastB = B.getLast();
                System.out.println("getlastA: " + lastA);
                System.out.println("getlastB: " + lastB);
                assertEquals(lastA, lastB);
            }
        }
    }



}
