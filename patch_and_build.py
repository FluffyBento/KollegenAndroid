import subprocess
import os

ndk_path = "/home/fluffybento/Android/Sdk/ndk"
ndk_versions = sorted(os.listdir(ndk_path), reverse=True)
print("Available NDK versions:", ndk_versions)

android_mk_path = "app_pojavlauncher/src/main/jni/Android.mk"
with open(android_mk_path, "r", encoding="utf-8") as f:
    content = f.read()

target_injection = "LOCAL_SHARED_LIBRARIES += androidnsbypass\nLOCAL_MODULE := pojavexec"

if target_injection not in content:
    print("Injecting androidnsbypass into Android.mk for pojavexec")
    content = content.replace(
        "LOCAL_MODULE := pojavexec",
        target_injection
    )
    with open(android_mk_path, "w", encoding="utf-8") as f:
        f.write(content)

build_cmd = (
    "export JAVA_HOME=/usr/lib/jvm/java-25-openjdk && "
    "export PATH=$JAVA_HOME/bin:$PATH && "
    "./gradlew :app_pojavlauncher:assembleDebug --max-workers 1 -Djava.io.tmpdir=/home/fluffybento/gradle_tmp"
)

subprocess.run(build_cmd, shell=True, check=True)
