package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class IntentTest {
    @Test
    public void testStringExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", "bar"));
        assertEquals("bar", intent.getExtras().get("foo"));
    }

    @Test
    public void testIntExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", 2));
        assertEquals(2, intent.getExtras().get("foo"));
        assertEquals(2, intent.getIntExtra("foo", -1));
    }

    @Test
    public void testSerializableExtra() throws Exception {
        Intent intent = new Intent();
        TestSerializable serializable = new TestSerializable("some string");
        assertSame(intent, intent.putExtra("foo", serializable));
        assertEquals(serializable, intent.getExtras().get("foo"));
        assertNotSame(serializable, intent.getExtras().get("foo"));
        assertEquals(serializable, intent.getSerializableExtra("foo"));
        assertNotSame(serializable, intent.getSerializableExtra("foo"));
    }

    @Test
    public void testParcelableExtra() throws Exception {
        Intent intent = new Intent();
        Parcelable parcelable = new TestParcelable();
        assertSame(intent, intent.putExtra("foo", parcelable));
        assertSame(parcelable, intent.getExtras().get("foo"));
        assertSame(parcelable, intent.getParcelableExtra("foo"));
    }

    @Test
    public void testParcelableArrayListExtra() {
        Intent intent = new Intent();
        Parcelable parcel1 = new TestParcelable();
        Parcelable parcel2 = new TestParcelable();
    	ArrayList<Parcelable> parcels = new ArrayList<Parcelable>();
    	parcels.add(parcel1);
    	parcels.add(parcel2);
    	
    	assertSame(intent, intent.putParcelableArrayListExtra("foo", parcels));
    	assertSame(parcels, intent.getParcelableArrayListExtra("foo"));
    	assertSame(parcel1, intent.getParcelableArrayListExtra("foo").get(0));
    	assertSame(parcel2, intent.getParcelableArrayListExtra("foo").get(1));
    	assertSame(parcels, intent.getExtras().getParcelableArrayList("foo"));
    }
    
    @Test
    public void testLongExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", 2L));
        assertEquals(2L, shadowOf(intent).getExtras().get("foo"));
        assertEquals(2L, intent.getLongExtra("foo", -1));
        assertEquals(-1L, intent.getLongExtra("bar", -1));
    }

    @Test
    public void testHasExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", ""));
        assertTrue(intent.hasExtra("foo"));
        assertFalse(intent.hasExtra("bar"));
    }

    @Test
    public void testGetActionReturnsWhatWasSet() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.setAction("foo"));
        assertEquals("foo", intent.getAction());
    }

    @Test
    public void testSetData() throws Exception {
        Intent intent = new Intent();
        Uri uri = Uri.parse("content://this/and/that");
        assertSame(intent, intent.setData(uri));
        assertSame(uri, intent.getData());
    }

    @Test
    public void testSetClass() throws Exception {
        Intent intent = new Intent();
        Class<? extends IntentTest> thisClass = getClass();
        Intent output = intent.setClass(new Activity(), thisClass);

        assertSame(output, intent);
        ShadowIntent si = shadowOf(intent);
        assertSame(si.getIntentClass(), thisClass);
    }

    @Test
    public void testSetClassName() throws Exception {
        Intent intent = new Intent();
        Class<? extends IntentTest> thisClass = getClass();
        intent.setClassName("package.name", thisClass.getName());
        assertSame(thisClass.getName(), intent.getComponent().getClassName());
        assertEquals("package.name", intent.getComponent().getPackageName());
    }

    @Test
    public void shouldSetFlags() throws Exception {
        Intent intent = new Intent();
        Intent self = intent.setFlags(1234);
        assertEquals(1234, intent.getFlags());
        assertSame(self, intent);
    }
    
    @Test
    public void shouldAddFlags() throws Exception {
        Intent intent = new Intent();
        Intent self = intent.addFlags(1234);
        assertEquals(1234, intent.getFlags());
        assertSame(self, intent);    	
    }
    
    @Test
    public void shouldSupportCategories() throws Exception {
        Intent intent = new Intent();
        Intent self = intent.addCategory("category.name.1");
        intent.addCategory("category.name.2");
        
        assertTrue(intent.hasCategory("category.name.1"));
        assertTrue(intent.hasCategory("category.name.2"));
        
        Set<String> categories = intent.getCategories();
        assertTrue( categories.contains("category.name.1") );
        assertTrue( categories.contains("category.name.2") );
        
        intent.removeCategory("category.name.1");
        assertFalse(intent.hasCategory("category.name.1"));
        assertTrue(intent.hasCategory("category.name.2"));

        intent.removeCategory("category.name.2");
        assertFalse(intent.hasCategory("category.name.2"));
        
        assertEquals(0, intent.getCategories().size() );
        
        assertSame(self, intent);
    }
    

    @Test
    public void equals_shouldTestActionComponentNameDataAndExtras() throws Exception {
        Intent intentA = new Intent()
                .setAction("action")
                .setData(Uri.parse("content:1"))
                .setComponent(new ComponentName("pkg", "cls"))
                .putExtra("extra", "blah")
                .setType("image/*");

        Intent intentB = new Intent()
                .setAction("action")
                .setData(Uri.parse("content:1"))
                .setComponent(new ComponentName("pkg", "cls"))
                .putExtra("extra", "blah")
                .setType("image/*");

        assertThat(intentA, equalTo(intentB));

        intentB.setAction("other action");
        assertThat(intentA, not(equalTo(intentB)));

        intentB.setAction("action");
        intentB.setData(Uri.parse("content:other"));
        assertThat(intentA, not(equalTo(intentB)));

        intentB.setData(Uri.parse("content:1"));
        intentB.setComponent(new ComponentName("other-pkg", "other-cls"));
        assertThat(intentA, not(equalTo(intentB)));

        intentB.setComponent(new ComponentName("pkg", "cls"));
        intentB.putExtra("extra", "foo");
        assertThat(intentA, not(equalTo(intentB)));

        intentB.putExtra("extra", "blah");
        intentB.setType("other/*");
        assertThat(intentA, not(equalTo(intentB)));

        intentB.setType("image/*");
        assertThat(intentA, equalTo(intentB));
    }

    @Test
    public void equals_whenOtherObjectIsNotAnIntent_shouldReturnFalse() throws Exception {
        assertThat((Object) new Intent(), not(equalTo(new Object())));
    }

    @Test
    public void createChooser_shouldWrapIntent() throws Exception {
        Intent originalIntent = new Intent(Intent.ACTION_BATTERY_CHANGED, Uri.parse("foo://blah"));
        Intent chooserIntent = Intent.createChooser(originalIntent, "The title");
        Intent expectedIntent = new Intent(Intent.ACTION_CHOOSER);
        expectedIntent.putExtra(Intent.EXTRA_INTENT, originalIntent);
        expectedIntent.putExtra(Intent.EXTRA_TITLE, "The title");
        assertEquals(expectedIntent, chooserIntent);
    }

    private static class TestSerializable implements Serializable {
        private String someValue;

        public TestSerializable(String someValue) {
            this.someValue = someValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSerializable that = (TestSerializable) o;

            if (someValue != null ? !someValue.equals(that.someValue) : that.someValue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return someValue != null ? someValue.hashCode() : 0;
        }
    }

    private class TestParcelable implements Parcelable {
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
