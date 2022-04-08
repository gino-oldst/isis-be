#Attachment handling for Bloomberg data
#Status
Accepted

#Context
Bloomberg has two kinds of data, email and chat. Attachments are handled/structured differently on both data sources.

_Bloomberg Chat Data XML structure_
```
<FileDump>
    <Conversation>
        <Message>
            <User />
            <Content />
            <DateTimeUTC />
            <Content />
            <ConversationID /> 
        </Message>
        <Attachment>
            <User />
            <DateTimeUTC />
            <ConversationID />
            <FileName />
            <FileID />
            <FileSize />
        </Attachment>
    </Conversation>
    <Conversation>
        ...
    </Conversation>
</FileDump>
```

_Bloomberg Email Data XML Structure_

```
<FileDump>
    <Message>
        <MsgID />
        <MsgBody />
        ...
        <Attachment>
            <FileName />
            <FileID />
            <FileSize />
            <Reference />
        </Attachment>
    </Message>
    <Message>
        ...
    </Message>
</FileDump>
```

In chat, attachments are treated as messages and structured separately from messages, the only thing both have in common is the conversationId.

On the other hand, email attachments are structured inside a particular message

#Decision
The proponents of the change decided to add `type` and `metadata` fields in CommunicationMessage entity.
* `Type` will describe what kind of message a CommunicationMessage object is.
e.g. it could be `BLOOMBERG_CHAT_ATTACHMENT`, `BLOOMBERG_EMAIL` and etc.
* `Metadata` will contain all the unique fields or data for a specific type. e.g. in the case of BLOOMBERG_CHAT_ATTACHMENT, the metadata will contain the fields `filename`, `fileId` and `fileSize`.

This will make the communication message flexible and could handle any types in future changes.