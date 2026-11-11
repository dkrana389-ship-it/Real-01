# Reel Counter (Android)

Instagram pe jaise-jaise reel scroll hoti hai, upar floating counter badhta hai.
Counter sirf Instagram khulne par dikhta hai, band hote hi gayab. Din ki counting save rehti hai.

## Chalane ke steps
1. Android Studio (Koala ya naya) install karo.
2. File → Open → is `ReelCounter` folder ko select karo. Gradle sync hone do (internet chahiye).
3. Phone USB debugging ON karke connect karo → Run (green play button).
   APK chahiye to: Build → Build Bundle(s)/APK(s) → Build APK(s).
4. App kholo → "Enable karo" → Accessibility mein Reel Counter ON karo.
5. Instagram kholo → Reels scroll karo.

## Agar count galat ho
Instagram apne andar ke naam badalta rehta hai. App ke Settings mein "Sirf Reels count karo" OFF karke dekho.
Phir bhi problem ho to Cursor mein `ReelAccessibilityService.kt` kholke ye prompt do:
"Instagram reels scroll detection fix karo, count har naye reel pe sirf +1 hona chahiye."

## Play Store ke liye
Accessibility permission ka Declaration form bharna padega: purpose = "Instagram reels ginti aur screen time dikhana".
Privacy Policy mein likho ki koi data collect/share nahi hota. Package name `com.deepinnovations.reelcounter` apne hisaab se badal sakte ho.

## GitHub par build (Android Studio ke bina)
1. github.com par naya repository banao (naam: ReelCounter).
2. "Add file → Upload files" se is folder ke saare files (`.github` folder samet) upload karo, phir Commit.
3. Repository mein **Actions** tab kholo → "Build APK" chal jayega (5-10 min).
4. Build poori hone par us run ko kholo → neeche **Artifacts → ReelCounter-APK** download karo → zip ke andar `app-debug.apk`.
5. APK phone mein install karo (Unknown sources allow karna padega).
Error aaye to Actions ka red run kholke error text Cursor/Claude ko paste karo.
