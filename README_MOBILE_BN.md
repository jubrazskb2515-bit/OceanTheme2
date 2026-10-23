# Dark Glass Water — Android APK project

এই প্রজেক্টটি Infinix Hot 50 Pro / Android 15-এ ইনস্টল করে পরীক্ষা করার জন্য তৈরি করা একটি standalone prototype। এতে accelerometer-ভিত্তিক পানির ঢেউ, neon glass panels, এবং “Better Things Ahead” কার্ডে ট্যাপ করলে নোট লিখে ফোনে সেভ করার সুবিধা আছে। বিজ্ঞাপন, সাবস্ক্রিপশন বা ইন্টারনেট permission নেই।

## APK build (GitHub Actions, মোবাইল থেকেও)
1. ZIP extract করুন এবং সব ফাইল—বিশেষ করে `.github/workflows/build.yml`—একটি GitHub repository-তে upload করুন।
2. Repository → **Actions** → **Build Dark Glass Water APK** → **Run workflow**।
3. কাজ শেষ হলে workflow run খুলে **Artifacts** থেকে `DarkGlassWaterTheme-debug-apk` নামের ZIP নামান।
4. ZIP extract করে `app-debug.apk` ফোনে ইনস্টল করুন।

## গুরুত্বপূর্ণ সীমাবদ্ধতা
- এই ZIP হলো build-ready source project; এখানে APK compile করে পরীক্ষা করা হয়নি।
- এটি এখনও XOS-এর আসল home screen, lock screen, notification shade, keyboard বা system Settings প্রতিস্থাপন করে না। এটি একটি theme/demo app; পুরো ফোনের UI বদলাতে Android launcher এবং system-level সীমাবদ্ধতার জন্য আলাদা কাজ দরকার।
- বর্তমান prototype-এ সেন্সর পানির ঢেউ/লেভেলকে প্রভাবিত করে এবং Notes কার্ডে ট্যাপ করে নোট সেভ করা যায়; সব আইকনের free-drag/page creation এখনও সম্পূর্ণ করা হয়নি।
