param([Parameter(Mandatory=$true)][string]$Path)

Add-Type -AssemblyName System.IO.Compression.FileSystem
$temp = Join-Path $env:TEMP ("docx_edit_" + [guid]::NewGuid().ToString("N"))
[IO.Directory]::CreateDirectory($temp) | Out-Null
[IO.Compression.ZipFile]::ExtractToDirectory($Path, $temp)

$xmlPath = Join-Path $temp 'word\document.xml'
[xml]$doc = Get-Content -LiteralPath $xmlPath -Raw -Encoding UTF8
$ns = [Xml.XmlNamespaceManager]::new($doc.NameTable)
$ns.AddNamespace('w', 'http://schemas.openxmlformats.org/wordprocessingml/2006/main')

$stackText = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('S290bGluIChBbmRyb2lkIE5hdGl2ZSkgdsOgIEpldHBhY2sgQ29tcG9zZQ=='))
$skillText = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('TsOibmcgY2FvIGvhu7kgbsSDbmcgcGjDoXQgdHJp4buDbiDhu6luZyBk4bulbmcgQW5kcm9pZCBuYXRpdmUgYuG6sW5nIEtvdGxpbiB2w6AgSmV0cGFjayBDb21wb3NlLg=='))

foreach ($paragraph in $doc.SelectNodes('//w:p', $ns)) {
    $textNodes = $paragraph.SelectNodes('.//w:t', $ns)
    if ($textNodes.Count -eq 0) { continue }
    $text = ($textNodes | ForEach-Object { $_.InnerText }) -join ''
    $replacement = $null
    if ($text -eq 'React Native (CLI)') { $replacement = $stackText }
    elseif ($text -match '^N.*mobile') { $replacement = $skillText }
    if ($null -ne $replacement) {
        $textNodes[0].InnerText = $replacement
        for ($i = 1; $i -lt $textNodes.Count; $i++) { $textNodes[$i].InnerText = '' }
    }
}

$settings = [Xml.XmlWriterSettings]::new()
$settings.Encoding = [Text.UTF8Encoding]::new($false)
$settings.Indent = $false
$writer = [Xml.XmlWriter]::Create($xmlPath, $settings)
$doc.Save($writer)
$writer.Dispose()

$newPath = "$Path.new"
if (Test-Path $newPath) { Remove-Item -LiteralPath $newPath -Force }
[IO.Compression.ZipFile]::CreateFromDirectory($temp, $newPath)
Move-Item -LiteralPath $newPath -Destination $Path -Force
Remove-Item -LiteralPath $temp -Recurse -Force
