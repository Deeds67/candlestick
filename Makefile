test:
	docker-compose up -d --wait
	./gradlew flywayMigrate
	./gradlew clean test
	docker compose down

run:
	docker-compose up -d --wait
	./gradlew flywayMigrate
	./gradlew run
