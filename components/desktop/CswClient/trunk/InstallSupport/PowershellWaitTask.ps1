param(
	[Parameter(Mandatory=$true)][string]$ManualBuildStepFinishedDir = 'ManualBuildStepFinishedTemp',
	[Parameter(Mandatory=$true)][string]$ManualBuildStepFinishedFile = 'ManualStepComplete.txt',
	[Parameter(Mandatory=$false)][string]$ManualBuildStepWaitTimeoutHours = '24'	
)

$agentBuildDir = $Env:AGENT_BUILDDIRECTORY
$timesofar = 0
$WorkingFolder = "$agentBuildDir\$ManualBuildStepFinishedDir"
$ManualStepComplete = "$WorkingFolder\$ManualBuildStepFinishedFile"

[double]$timeout = ([convert]::ToDouble($ManualBuildStepWaitTimeoutHours) * 3600)
if (!(Test-Path -PathType Container -Path $WorkingFolder)){
    New-Item -ItemType Directory -Force -Path $WorkingFolder
}

Remove-Item "$WorkingFolder\*"
$start = Get-Date -format "dd-MMM-yyyy HH:mm"
Write-Host "ManualBuildStepWaitTimeoutHours: $ManualBuildStepWaitTimeoutHours"
Write-Host "WorkingFolder: $WorkingFolder"
Write-Host "ManualBuildStepFinishedFile: $ManualBuildStepFinishedFile"
Write-Host "Manual Wait Step Triggered at: $start"

while (!(Test-Path $ManualStepComplete) -and !($timesofar -gt $timeout)) 
{ 
    #Wait for 10 seconds
    Start-Sleep 10 
    #Add 10 seconds to the time So Far
    $timesofar = $timesofar + 10
}
 
#Either the File has been dropped by the other system or the timeout was reached
if (!(Test-Path $ManualStepComplete)) 
{
    #If the file was not dropped it must be the timeout so fail the build write to the console
    Write-Error "Wait Step Timed out after $timeout seconds."
	Exit 1
}
else
{
    Write-Verbose "Successful Wait"    
}

#Remove directory
Remove-Item -LiteralPath $WorkingFolder -Force -Recurse

