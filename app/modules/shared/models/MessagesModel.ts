module.exports = {
    fields:{
        id:{
            type: "uuid",
            default: {"$db_function": "uuid()"}
        },
        broadcast_type:'text',
        created_on:'timestamp',
        message_data: 'text',
        message_type: 'text',
        recipient_refid: 'text',
        recipient_reftype: 'text',
        status: 'text',
        template_name: 'text',
        updated_on: 'timestamp'    
    },
    key:["id"],
    table_name: "messages"
}
