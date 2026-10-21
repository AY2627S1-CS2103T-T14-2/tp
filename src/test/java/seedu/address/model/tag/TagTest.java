package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void isSameTag() {
        Tag vip = new Tag("vip");

        // same name, ignoring case -> returns true
        assertTrue(vip.isSameTag(new Tag("vip")));
        assertTrue(vip.isSameTag(new Tag("VIP")));

        // different name -> returns false
        assertFalse(vip.isSameTag(new Tag("vips")));

        // null -> returns false
        assertFalse(vip.isSameTag(null));
    }

    @Test
    public void alphabeticalOrder() {
        // case is ignored, except to order tags that differ only in case
        assertTrue(Tag.ALPHABETICAL_ORDER.compare(new Tag("apple"), new Tag("Banana")) < 0);
        assertTrue(Tag.ALPHABETICAL_ORDER.compare(new Tag("VIP"), new Tag("vip")) < 0);
        assertTrue(Tag.ALPHABETICAL_ORDER.compare(new Tag("vip"), new Tag("vip")) == 0);
    }

    @Test
    public void sortedDistinctIgnoringCase() {
        // sorted alphabetically, keeping the first of tags that differ only in case
        assertEquals(List.of(new Tag("donor"), new Tag("VIP")),
                Tag.sortedDistinctIgnoringCase(Set.of(new Tag("vip"), new Tag("donor"), new Tag("VIP"))));

        // no tags
        assertEquals(List.of(), Tag.sortedDistinctIgnoringCase(Set.of()));

        // null
        assertThrows(NullPointerException.class, () -> Tag.sortedDistinctIgnoringCase(null));
    }

}
