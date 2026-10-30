#!/usr/bin/env bash
# Consumer check: build and run a throwaway project (copied outside the repo) that depends on
# io.github.sanamhub:nepdate and io.github.sanamhub:nepdate-patro, converts one date and reads one
# patro event, and checks the printed lines.
#
#   tools/consumer-check/run.sh                              publish to mavenLocal, resolve from there
#   tools/consumer-check/run.sh --published 0.1.0-beta.1 [--wait-minutes 45]
#   tools/consumer-check/run.sh --published latest --resolve
#   tools/consumer-check/run.sh --published latest --metadata
#   tools/consumer-check/run.sh --published 0.1.0-beta.1 --manager maven
#
# The default proves the coordinates, POMs and README install block of this checkout. --published
# proves a released version: no mavenLocal, only Maven Central, retried until Central serves it,
# because a new release can take 10 to 30 minutes to appear. --manager picks the build tool:
# gradle is the Kotlin DSL project here, maven a small pom that compiles the same program.
# --resolve prints the version Maven Central serves alone and exits; --metadata checks that Central
# still lists both artifacts and sends HEAD requests to their pom and jar, downloading neither.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CENTRAL="https://repo1.maven.org/maven2"
GROUP_PATH="io/github/sanamhub"

published=""
wait_minutes=0
manager=gradle
mode=install
while [ $# -gt 0 ]; do
  case "$1" in
    --published) published="${2:?--published needs a version}"; shift 2 ;;
    --wait-minutes) wait_minutes="${2:?--wait-minutes needs a number}"; shift 2 ;;
    --manager) manager="${2:?--manager needs gradle or maven}"; shift 2 ;;
    --resolve) mode=resolve; shift ;;
    --metadata) mode=metadata; shift ;;
    *) echo "consumer-check: unknown argument '$1'" >&2; exit 2 ;;
  esac
done

fetch() {
  curl -fsS --retry 3 -H "User-Agent: nepdate-verify-published" "$1"
}

# The version a plain `implementation("io.github.sanamhub:nepdate:<version>")` picks from Central:
# the newest release, which includes pre-releases while every release is one.
release_version() {
  local artifact="$1" xml version
  xml="$(fetch "${CENTRAL}/${GROUP_PATH}/${artifact}/maven-metadata.xml")"
  version="$(printf '%s' "$xml" | tr -d '\n' | sed -n 's:.*<release>\([^<]*\)</release>.*:\1:p')"
  if [ -z "$version" ]; then
    version="$(printf '%s' "$xml" | tr -d '\n' | sed -n 's:.*<latest>\([^<]*\)</latest>.*:\1:p')"
  fi
  printf '%s' "$version"
}

VERSION="$published"
if [ "$mode" = resolve ] || [ "$mode" = metadata ]; then
  VERSION="${VERSION:-latest}"
fi
if [ "$VERSION" = latest ]; then
  VERSION="$(release_version nepdate)"
  if [ "$mode" != resolve ]; then
    echo "consumer-check: Maven Central latest is ${VERSION}"
  fi
fi
if [ "$mode" = resolve ]; then
  printf '%s\n' "$VERSION"
  exit 0
fi

# The daily check: Central still lists the version and serves each artifact's pom and jar. A HEAD
# request downloads no jar, so the check adds nothing to Central's download count.
if [ "$mode" = metadata ]; then
  if [ -z "$VERSION" ] || [ "$VERSION" = latest ]; then
    echo "consumer-check: --metadata needs a version"
    exit 2
  fi
  for artifact in nepdate nepdate-patro; do
    xml="$(fetch "${CENTRAL}/${GROUP_PATH}/${artifact}/maven-metadata.xml")"
    if ! printf '%s' "$xml" | tr -d '\n' | grep -q "<version>${VERSION}</version>"; then
      echo "consumer-check: Maven Central does not list ${artifact} ${VERSION}"
      exit 1
    fi
    for extension in pom jar; do
      url="${CENTRAL}/${GROUP_PATH}/${artifact}/${VERSION}/${artifact}-${VERSION}.${extension}"
      code="$(curl -sS -o /dev/null -w '%{http_code}' -I --retry 3 "$url")"
      if [ "$code" != 200 ]; then
        echo "consumer-check: ${url} answered ${code}"
        exit 1
      fi
    done
    echo "consumer-check: ${artifact} ${VERSION} is listed and its pom and jar answer 200"
  done
  exit 0
fi

if [ "$manager" != gradle ] && [ "$manager" != maven ]; then
  echo "consumer-check: --manager must be gradle or maven, not '${manager}'" >&2
  exit 2
fi

# The throwaway project lives outside the repo; override CONSUMER_DIR to pick a location.
CONSUMER_DIR="${CONSUMER_DIR:-${RUNNER_TEMP:-${TMPDIR:-/tmp}}}/kt-consumer"

check_output() {
  local output="$1"
  # AD 2024-04-13 is BS 2081-01-01, and its first event is New Year. The Nepali text in
  # parentheses is printed for a reader but not compared, and its bytes may not survive a console
  # code page, so only the line's start is checked.
  if ! printf '%s\n' "$output" | tr -d '\r' | grep -q '^converted: 2081-01-01 ('; then
    echo "consumer-check: the conversion line is wrong" >&2
    return 1
  fi
  if ! printf '%s\n' "$output" | tr -d '\r' | grep -q '^event: New Year ('; then
    echo "consumer-check: the patro line is wrong" >&2
    return 1
  fi
}

# Maven builds the same Kotlin program from one pom, so the check does not depend on a Gradle
# plugin resolving from the network.
run_maven() {
  if [ -z "$published" ]; then
    echo "consumer-check: --manager maven needs --published, because there is no local pom to use" >&2
    exit 2
  fi
  local dir="${CONSUMER_DIR}-maven"
  rm -rf "$dir"
  mkdir -p "$dir/src/main/kotlin/consumer"
  cp "$SCRIPT_DIR/src/main/kotlin/consumer/Main.kt" "$dir/src/main/kotlin/consumer/Main.kt"
  cat > "$dir/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>consumer</groupId>
  <artifactId>nepdate-consumer</artifactId>
  <version>1.0</version>
  <properties>
    <kotlin.version>2.4.20</kotlin.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <dependencies>
    <dependency>
      <groupId>io.github.sanamhub</groupId>
      <artifactId>nepdate</artifactId>
      <version>${VERSION}</version>
    </dependency>
    <dependency>
      <groupId>io.github.sanamhub</groupId>
      <artifactId>nepdate-patro</artifactId>
      <version>${VERSION}</version>
    </dependency>
  </dependencies>
  <build>
    <sourceDirectory>src/main/kotlin</sourceDirectory>
    <plugins>
      <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
        <version>\${kotlin.version}</version>
        <executions>
          <execution>
            <id>compile</id>
            <phase>process-sources</phase>
            <goals><goal>compile</goal></goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.5.0</version>
        <configuration><mainClass>consumer.MainKt</mainClass></configuration>
      </plugin>
    </plugins>
  </build>
</project>
EOF
  local options=()
  if [ -n "${NEPDATE_MAVEN_REPO:-}" ]; then options+=("-Dmaven.repo.local=${NEPDATE_MAVEN_REPO}"); fi
  echo "== resolve ${VERSION} from Maven Central with Maven ==" >&2
  local output
  if ! output="$(cd "$dir" && mvn -q -B "${options[@]}" compile exec:java 2>&1)"; then
    printf '%s\n' "$output" >&2
    echo "consumer-check: the Maven consumer failed" >&2
    exit 1
  fi
  printf '%s\n' "$output"
  check_output "$output"
  echo "consumer-check: ok: ${VERSION} from Maven Central with Maven"
}

if [ "$manager" = maven ]; then
  run_maven
  exit 0
fi

cd "$REPO_ROOT"

if [ -n "$published" ]; then
  SOURCE=central
else
  VERSION="$(grep '^version=' gradle.properties | cut -d= -f2 | tr -d '[:space:]')"
  SOURCE=local
  echo "== publish $VERSION to mavenLocal =="
  ./gradlew --console=plain publishToMavenLocal
fi

echo "== copy the consumer out of the repo =="
rm -rf "$CONSUMER_DIR"
mkdir -p "$CONSUMER_DIR"
cp -r "$SCRIPT_DIR/settings.gradle.kts" "$SCRIPT_DIR/build.gradle.kts" "$SCRIPT_DIR/src" \
    "$CONSUMER_DIR/"

# The repo's own wrapper drives the copy (-p), so the consumer carries no second wrapper jar. The
# version and the repository choice reach the consumer build as Gradle properties.
GRADLE=("$REPO_ROOT/gradlew" --console=plain -p "$CONSUMER_DIR"
  "-PnepdateVersion=$VERSION" "-PnepdateSource=$SOURCE")

echo "== resolve $VERSION from $SOURCE =="
deadline=$(( $(date +%s) + wait_minutes * 60 ))
while true; do
  if resolved="$("${GRADLE[@]}" -q --refresh-dependencies printNepdatePath 2>&1)"; then
    break
  fi
  if [ "$(date +%s)" -ge "$deadline" ]; then
    printf '%s\n' "$resolved" >&2
    echo "consumer-check: $VERSION did not resolve from $SOURCE" >&2
    exit 1
  fi
  echo "consumer-check: $VERSION not served by $SOURCE yet, retrying in 60s"
  sleep 60
done
printf '%s\n' "$resolved" >&2
# Both jars must be on the classpath at exactly this version.
for artifact in nepdate nepdate-patro; do
  if ! printf '%s\n' "$resolved" | grep -qE "[\\/]$artifact-${VERSION//./\\.}\.jar\$"; then
    echo "consumer-check: $artifact-$VERSION.jar is not on the classpath" >&2
    exit 1
  fi
done
# A checkout's jars come from the local repository; a release's never do.
if [ "$SOURCE" = local ] && ! printf '%s\n' "$resolved" | grep -q '[\\/]\.m2[\\/]'; then
  echo "consumer-check: the jars did not come from mavenLocal" >&2
  exit 1
fi
if [ "$SOURCE" = central ] && printf '%s\n' "$resolved" | grep -q '[\\/]\.m2[\\/]'; then
  echo "consumer-check: a released check resolved from mavenLocal" >&2
  exit 1
fi

echo "== run the consumer =="
output="$("${GRADLE[@]}" -q run)"
printf '%s\n' "$output"
check_output "$output"
echo "consumer-check: ok: $VERSION from $SOURCE"
