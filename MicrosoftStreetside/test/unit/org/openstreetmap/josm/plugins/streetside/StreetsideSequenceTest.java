// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Tests for the {@link StreetsideSequence} class.
 *
 * @author nokutu
 * @see StreetsideSequence
 */
class StreetsideSequenceTest {

  private final StreetsideImage img1 = new StreetsideImage("key1", new LatLon(0.1, 0.1), 90);
  private final StreetsideImage img2 = new StreetsideImage("key2", new LatLon(0.2, 0.2), 90);
  private final StreetsideImage img3 = new StreetsideImage("key3", new LatLon(0.3, 0.3), 90);
  private final StreetsideImage img4 = new StreetsideImage("key4", new LatLon(0.4, 0.4), 90);
  private final StreetsideImage imgWithoutSeq = new StreetsideImage("key5", new LatLon(0.5, 0.5), 90);
  private final StreetsideSequence seq = new StreetsideSequence();

  /**
   * Creates 4 {@link StreetsideImage} objects and puts them in a
   * {@link StreetsideSequence} object.
   */
  @BeforeEach
  public void setUp() {
    seq.add(Arrays.asList(img1, img2, img3, img4));
  }

  /**
   * Tests the {@link StreetsideSequence#next(StreetsideAbstractImage)} and
   * {@link StreetsideSequence#previous(StreetsideAbstractImage)}.
   */
  @Test
  void testNextAndPrevious() {
    assertEquals(img2, img1.next());
    assertEquals(img1, img2.previous());
    assertEquals(img3, img2.next());
    assertEquals(img2, img3.previous());
    assertEquals(img4, img3.next());
    assertEquals(img3, img4.previous());


    assertNull(img4.next());
    assertNull(img1.previous());

    assertNull(imgWithoutSeq.next());
    assertNull(imgWithoutSeq.previous());

    // Test IllegalArgumentException when asking for the next image of an image
    // that is not in the sequence.
    assertThrows(IllegalArgumentException.class, () -> seq.next(imgWithoutSeq));

    // Test IllegalArgumentException when asking for the previous image of an
    // image that is not in the sequence.
    assertThrows(IllegalArgumentException.class, () -> seq.previous(imgWithoutSeq));
  }
}
