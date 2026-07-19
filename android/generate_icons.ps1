Add-Type -AssemblyName System.Drawing
$res = "$pwd\app\src\main\res"

$sizes = @{
    "mdpi" = 48
    "hdpi" = 72
    "xhdpi" = 96
    "xxhdpi" = 144
    "xxxhdpi" = 192
}

foreach ($dpi in $sizes.Keys) {
    $dir = "$res\mipmap-$dpi"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    
    $size = $sizes[$dpi]
    
    # ic_launcher
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Teal)
    $bmp.Save("$dir\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    
    # ic_launcher_round
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Teal)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $g.FillEllipse($brush, 0, 0, $size, $size)
    $bmp.Save("$dir\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    $brush.Dispose()
}

# Adaptive icons
$anydpi = "$res\mipmap-anydpi-v26"
New-Item -ItemType Directory -Force -Path $anydpi | Out-Null

$adaptiveXml = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"@

Set-Content -Path "$anydpi\ic_launcher.xml" -Value $adaptiveXml
Set-Content -Path "$anydpi\ic_launcher_round.xml" -Value $adaptiveXml

$drawable = "$res\drawable"
New-Item -ItemType Directory -Force -Path $drawable | Out-Null

$bgXml = @"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#008080" android:pathData="M0,0h108v108h-108z"/>
</vector>
"@

$fgXml = @"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:pathData="M54,24 A30,30 0 1,1 54,84 A30,30 0 1,1 54,24 Z"/>
</vector>
"@

Set-Content -Path "$drawable\ic_launcher_background.xml" -Value $bgXml
Set-Content -Path "$drawable\ic_launcher_foreground.xml" -Value $fgXml
