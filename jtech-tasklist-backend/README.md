![Jtech Logo](http://www.jtech.com.br/wp-content/uploads/2015/06/logo.png)

# jtech-tasklist

## What is

## Composite by

## Services

## Helper

## How to use

## Sample

## How to run

## Points to improve

## Design Notes

- **Soft-delete lazy cascade**: Tasklist deletion is soft (`markAsDeleted` + `save`), not a hard DB delete. Child tasks are not mass-updated; instead, every task query/mutation use case verifies the parent tasklist exists before proceeding. This means each `GET /tasks?tasklistId=X` costs two DB round-trips (tasklist existence check + task fetch) instead of one — a deliberate trade-off of the lazy-cascade design at this app's scale.