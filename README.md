PhotoGallery
============
-PhotoGallery App displays photos in slideshow mode and photos mode.
-Downloads images from urls from strimg.xml if not present in cache.
-Disk Cache is used. 
-Implemented the cache refering 
http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
- Note that the bitmaps added in the cache are stored as key-value pairs. Each key must match the regex [a-z0-9_-]{1,64}. Hence cannot add url directly as the key to store bitmap in cache.

Setup - 
Used DiskLruCache Class  implementation from bitmap.zip from 
http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#memory-cache

