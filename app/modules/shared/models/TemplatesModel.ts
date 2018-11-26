module.exports = {
    fields: {
        id: {
            type: "uuid",
            default: { "$db_function": "uuid()" }
        },
        created_on: 'timestamp',
        name: 'text',
        status: 'text',
        template: 'text',
        updated_on: 'timestamp'
    },
    key: ["id"],
    table_name: "templates"
}
