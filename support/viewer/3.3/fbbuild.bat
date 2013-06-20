REM works with either FlashBuilderC.exe or eclipsec.exe
"%FB_PATH%" ^
    --launcher.suppressErrors ^
    -noSplash ^
    -application org.eclipse.ant.core.antRunner ^
    -data "%WORKSPACE%" ^
    -file "%cd%\fbbuild.xml" main
