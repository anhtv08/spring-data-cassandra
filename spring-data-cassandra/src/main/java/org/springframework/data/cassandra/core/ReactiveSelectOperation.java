/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.query.Query;

/**
 * {@link ReactiveSelectOperation} allows creation and execution of Cassandra {@code SELECT} operations in a fluent API
 * style.
 * <p>
 * The starting {@literal domainType} is used for mapping the {@link Query} provided via {@code matching} into the
 * Cassandra specific representation. By default, the originating {@literal domainType} is also used for mapping back
 * the result from the {@link com.datastax.driver.core.Row}. However, it is possible to define an different
 * {@literal returnType} via {@code as} to mapping the result.
 * <p>
 * The table to operate on is by default derived from the initial {@literal domainType} and can be defined there via
 * {@link org.springframework.data.cassandra.core.mapping.Table}. Using {@code inTable} allows to override the table
 * name for the execution.
 *
 * <pre>
 *     <code>
 *         query(Human.class)
 *             .inTable("star_wars")
 *             .as(Jedi.class)
 *             .matching(query(where("firstname").is("luke")))
 *             .all();
 *     </code>
 * </pre>
 *
 * @author Mark Paluch
 * @since 2.1
 */
public interface ReactiveSelectOperation {

	/**
	 * Start creating a {@code SELECT} operation for the given {@literal domainType}.
	 *
	 * @param domainType must not be {@literal null}.
	 * @return new instance of {@link ReactiveSelect}.
	 * @throws IllegalArgumentException if domainType is {@literal null}.
	 */
	<T> ReactiveSelect<T> query(Class<T> domainType);

	/**
	 * Trigger {@code SELECT} execution by calling one of the terminating methods.
	 */
	interface TerminatingSelect<T> {

		/**
		 * Get exactly zero or one result.
		 *
		 * @return {@link Mono#empty()} if no match found. Never {@literal null}.
		 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one match found.
		 */
		Mono<T> one();

		/**
		 * Get the first or no result.
		 *
		 * @return {@link Mono#empty()} if no match found. Never {@literal null}.
		 */
		Mono<T> first();

		/**
		 * Get all matching elements.
		 *
		 * @return never {@literal null}.
		 */
		Flux<T> all();

		/**
		 * Get the number of matching elements.
		 *
		 * @return {@link Mono} emitting total number of matching elements. Never {@literal null}.
		 */
		Mono<Long> count();

		/**
		 * Check for the presence of matching elements.
		 *
		 * @return {@link Mono} emitting {@literal true} if at least one matching element exists. Never {@literal null}.
		 */
		Mono<Boolean> exists();
	}

	/**
	 * Terminating operations invoking the actual query execution.
	 */
	interface SelectWithQuery<T> extends TerminatingSelect<T> {

		/**
		 * Set the filter query to be used.
		 *
		 * @param query must not be {@literal null}.
		 * @return new instance of {@link TerminatingSelect}.
		 * @throws IllegalArgumentException if query is {@literal null}.
		 */
		TerminatingSelect<T> matching(Query query);
	}

	/**
	 * Table override (Optional).
	 */
	interface SelectWithTable<T> extends SelectWithQuery<T> {

		/**
		 * Explicitly set the name of the table to perform the query on.
		 * <p>
		 * Skip this step to use the default table derived from the domain type.
		 *
		 * @param table must not be {@literal null} or empty.
		 * @return new instance of {@link SelectWithProjection}.
		 * @throws IllegalArgumentException if {@code table} is {@literal null} or empty.
		 */
		SelectWithProjection<T> inTable(String table);

		/**
		 * Explicitly set the name of the table to perform the query on.
		 * <p>
		 * Skip this step to use the default table derived from the domain type.
		 *
		 * @param table must not be {@literal null}.
		 * @return new instance of {@link SelectWithProjection}.
		 * @throws IllegalArgumentException if {@link CqlIdentifier} is {@literal null}.
		 */
		SelectWithProjection<T> inTable(CqlIdentifier table);
	}

	/**
	 * Result type override (Optional).
	 */
	interface SelectWithProjection<T> extends SelectWithQuery<T> {

		/**
		 * Define the target type fields should be mapped to. <br />
		 * Skip this step if you are anyway only interested in the original domain type.
		 *
		 * @param resultType must not be {@literal null}.
		 * @param <R> result type.
		 * @return new instance of {@link SelectWithProjection}.
		 * @throws IllegalArgumentException if resultType is {@literal null}.
		 */
		<R> SelectWithQuery<R> as(Class<R> resultType);
	}

	/**
	 * {@link ReactiveSelect} provides methods for constructing {@code SELECT} operations in a fluent way.
	 */
	interface ReactiveSelect<T> extends SelectWithTable<T>, SelectWithProjection<T> {}
}