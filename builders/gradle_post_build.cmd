@echo off
echo Starting of post building process...
if exist build\outputs\apk\Gallery-debug-unaligned.apk (
     copy AndroidManifest.xml bin\AndroidManifest.xml /Y
     copy build\outputs\apk\Gallery-debug-unaligned.apk bin\Gallery.apk /Y
) else (
	 echo Build error. Check the problems window for details...
)
	