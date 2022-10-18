test:
	docker-compose up postgres tests

run:
	docker-compose up java postgres candlesticks
