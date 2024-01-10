.PHONY: build publish clean refresh

build:
	./gradlew build

publish:
	./gradlew publish

publish-local:
	./gradlew publishToMavenLocal

test:
	./gradlew test

clean:
	./gradlew clean

refresh:
	./gradlew build --refresh-dependencies
