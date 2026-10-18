pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    parameters {
        string(name: 'HOST_PORT', defaultValue: '9091',
               description: 'Host port for the Docker container')
    }

    environment {
        REGISTRY       = 'localhost:5000'
        IMAGE_NAME     = 'research-booking-system'
        CONTAINER_NAME = 'research-booking-cd'
        // Clean docker config so the SYSTEM account never hits the Docker Desktop credential helper
        DOCKER_CONFIG  = "${WORKSPACE}\\.docker"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/swapnilsupe01/Research-Experiment-Booking.git'
            }
        }

        stage('Build and Test') {
            steps {
                dir('research-booking-system') {
                    bat 'mvn clean verify'
                }
            }
        }

        stage('Selenium Continuous Tests') {
            steps {
                writeFile file: 'selenium.ps1', text: '''
$ErrorActionPreference = "Stop"

$port = 8083
$jar = Join-Path $env:WORKSPACE "research-booking-system\\target\\research-booking-system-0.0.1-SNAPSHOT.jar"
$outLog = Join-Path $env:WORKSPACE "selenium-app.stdout.log"
$errLog = Join-Path $env:WORKSPACE "selenium-app.stderr.log"

if (!(Test-Path $jar)) { throw "JAR not found: $jar" }

if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
    throw "Port $port is already in use. Stop whatever is using it and re-run."
}

$java = (Get-Command java.exe).Source
$proc = Start-Process -FilePath $java `
    -ArgumentList "-jar", "`"$jar`"", "--server.port=$port" `
    -PassThru -WindowStyle Hidden `
    -RedirectStandardOutput $outLog -RedirectStandardError $errLog

function Get-Status($u) {
    try {
        return [int](Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 5).StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        return 0
    }
}

$exitCode = 1
try {
    $up = $false
    for ($i = 1; $i -le 40; $i++) {
        $code = Get-Status "http://localhost:$port/ui"
        Write-Host "Waiting for app on $port, attempt $i/40 -> HTTP $code"
        if ($code -gt 0 -and $code -lt 500) { $up = $true; break }
        if ($proc.HasExited) { break }
        Start-Sleep -Seconds 3
    }

    if (!$up) {
        Write-Host "---- app stdout (last 60) ----"
        Get-Content $outLog -Tail 60 -ErrorAction SilentlyContinue
        Write-Host "---- app stderr (last 60) ----"
        Get-Content $errLog -Tail 60 -ErrorAction SilentlyContinue
        throw "App did not start on port $port."
    }

    Push-Location (Join-Path $env:WORKSPACE "research-booking-system")
    & mvn -Dtest=SeleniumJourneyChecks test
    $exitCode = $LASTEXITCODE
    Pop-Location
}
finally {
    if ($proc -and !$proc.HasExited) {
        Stop-Process -Id $proc.Id -Force
        Start-Sleep -Seconds 3
    }
}

exit $exitCode
'''
                bat 'powershell.exe -NoProfile -ExecutionPolicy Bypass -File selenium.ps1'
            }
        }

        stage('Package') {
            steps {
                dir('research-booking-system') {
                    bat 'mvn package -DskipTests'
                }
            }
        }

        stage('Version') {
            steps {
                script {
                    env.GIT_SHORT     = bat(returnStdout: true, script: '@git rev-parse --short HEAD').trim()
                    env.IMAGE_VERSION = "1.0.${env.BUILD_NUMBER}"
                    env.FULL_IMAGE    = "${env.REGISTRY}/${env.IMAGE_NAME}"
                }
                echo "Image: ${env.FULL_IMAGE}:${env.IMAGE_VERSION} (commit ${env.GIT_SHORT})"
            }
        }

        stage('Docker Build') {
            steps {
                dir('research-booking-system') {
                    bat '''
docker version --format "Docker server {{.Server.Version}}" || exit /b 1
docker build -t %FULL_IMAGE%:%IMAGE_VERSION% -t %FULL_IMAGE%:latest --label git.commit=%GIT_SHORT% --label build.number=%BUILD_NUMBER% . || exit /b 1
docker images %FULL_IMAGE%
'''
                }
            }
        }

        stage('Push to Registry') {
            steps {
                bat '''
docker inspect registry >nul 2>&1 || docker run -d --restart unless-stopped -p 5000:5000 --name registry registry:2
docker start registry >nul 2>&1
docker push %FULL_IMAGE%:%IMAGE_VERSION% || exit /b 1
docker push %FULL_IMAGE%:latest || exit /b 1
echo ---- registry catalog ----
curl.exe -s http://localhost:5000/v2/_catalog || exit /b 1
echo.
echo ---- registry tags ----
curl.exe -s http://localhost:5000/v2/%IMAGE_NAME%/tags/list || exit /b 1
echo.
'''
            }
        }

        stage('Deploy Container') {
            steps {
                bat '''
docker rm -f %CONTAINER_NAME% >nul 2>&1
docker pull %FULL_IMAGE%:%IMAGE_VERSION% || exit /b 1
docker run -d --name %CONTAINER_NAME% --restart unless-stopped -p %HOST_PORT%:8080 -e SERVER_PORT=8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/research_booking_system" --label version=%IMAGE_VERSION% %FULL_IMAGE%:%IMAGE_VERSION% || exit /b 1
docker ps --filter name=%CONTAINER_NAME%
'''
            }
        }

        stage('Verify Container') {
            steps {
                writeFile file: 'verify-docker.ps1', text: '''
$ErrorActionPreference = "Continue"
$name = $env:CONTAINER_NAME
$base = "http://127.0.0.1:$env:HOST_PORT"

function Get-Status($u) {
    try {
        return [int](Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 5).StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        return 0
    }
}

$ok = $false
for ($i = 1; $i -le 40; $i++) {
    $api = Get-Status "$base/api/experiments"
    $ui  = Get-Status "$base/ui"
    Write-Host "Attempt $i/40: api=$api ui=$ui"
    if ($api -eq 200 -and $ui -eq 200) { $ok = $true; break }
    Start-Sleep -Seconds 3
}

$ev = Join-Path $env:WORKSPACE "cd-evidence.txt"
"Build $env:BUILD_NUMBER | Version $env:IMAGE_VERSION | Commit $env:GIT_SHORT | $(Get-Date)" | Out-File $ev
"---- docker ps ----" | Out-File $ev -Append
docker ps --filter "name=$name" 2>&1 | Out-File $ev -Append
"---- container image ----" | Out-File $ev -Append
docker inspect $name --format "Image: {{.Config.Image}} | Created: {{.Created}} | State: {{.State.Status}}" 2>&1 | Out-File $ev -Append
"---- registry tags ----" | Out-File $ev -Append
curl.exe -s "http://localhost:5000/v2/$env:IMAGE_NAME/tags/list" 2>&1 | Out-File $ev -Append
"---- HTTP checks ----" | Out-File $ev -Append
"api=$api ui=$ui" | Out-File $ev -Append
Get-Content $ev

if (!$ok) {
    Write-Host "---- container logs ----"
    docker logs --tail 60 $name
    throw "Container verification failed."
}
Write-Host "Container verification passed on port $env:HOST_PORT."
'''
                bat 'powershell.exe -NoProfile -ExecutionPolicy Bypass -File verify-docker.ps1'
            }
        }
    }

    post {
        always {
            junit(
                testResults: 'research-booking-system/target/surefire-reports/TEST-*.xml',
                allowEmptyResults: true
            )
            archiveArtifacts(
                artifacts: 'research-booking-system/target/*.jar',
                fingerprint: true,
                allowEmptyArchive: true
            )
            archiveArtifacts(
                artifacts: 'research-booking-system/target/selenium-screenshots/**',
                allowEmptyArchive: true
            )
            archiveArtifacts(
                artifacts: 'selenium-app.*.log,cd-evidence.txt',
                allowEmptyArchive: true
            )
        }
        success {
            echo "CD complete: ${env.FULL_IMAGE}:${env.IMAGE_VERSION} deployed as ${env.CONTAINER_NAME} on port ${params.HOST_PORT}."
        }
        failure {
            echo 'Pipeline failed. Check the Console Output of the first failing stage.'
        }
    }
}