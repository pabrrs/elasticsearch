/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.logical.command;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.xpack.sql.expression.Attribute;
import org.elasticsearch.xpack.sql.expression.FieldAttribute;
import org.elasticsearch.xpack.sql.session.Rows;
import org.elasticsearch.xpack.sql.session.SchemaRowSet;
import org.elasticsearch.xpack.sql.session.SqlSession;
import org.elasticsearch.xpack.sql.tree.Location;
import org.elasticsearch.xpack.sql.type.DataTypes;
import org.elasticsearch.xpack.sql.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ShowTables extends Command {

    @Nullable
    private final String pattern;

    public ShowTables(Location location, @Nullable String pattern) {
        super(location);
        this.pattern = pattern;
    }

    public String pattern() {
        return pattern;
    }

    @Override
    public List<Attribute> output() {
        return Collections.singletonList(new FieldAttribute(location(), "table", DataTypes.KEYWORD));
    }

    @Override
    public final void execute(SqlSession session, ActionListener<SchemaRowSet> listener) {
        String pattern = Strings.hasText(this.pattern) ? StringUtils.jdbcToEsPattern(this.pattern) : "*";
        session.indexResolver().asList(pattern, ActionListener.wrap(result -> {
            listener.onResponse(Rows.of(output(), result.stream()
                .map(t -> singletonList(t.name()))
                .collect(toList())));
        }, listener::onFailure));
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ShowTables other = (ShowTables) obj;
        return Objects.equals(pattern, other.pattern);
    }
}
