/*
 * LEGAL NOTICE
 *
 *  Copyright (C) ${year} InventIt Inc. All rights reserved.
 *
 *  This source code, product and/or document is protected under licenses
 *  restricting its use, copying, distribution, and decompilation.
 *  No part of this source code, product or document may be reproduced in
 *  any form by any means without prior written authorization of InventIt Inc.
 *  and its licensors, if any.
 *
 *  InventIt Inc.
 *  9F Kojimachi DUPLEX B's
 *  4-4-7 Kojimachi, Chiyoda-ku, Tokyo 102-0083
 *  JAPAN
 *  http://www.yourinventit.com/
 */

package com.enterpriseios.push;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/07/12
 * Time: 15:04:17
 * To change this template use File | Settings | File Templates.
 */
public class Base64CommandCodes {

    private final static String[] commands = {"Sync", // 0
            "SendMail", // 1
            "SmartForward", // 2
            "SmartReply", // 3
            "GetAttachment", // 4
            "GetHierarchy", // 5
            "CreateCollection", // 6
            "DeleteCollection", // 7
            "MoveCollection", // 8
            "FolderSync", // 9
            "FolderCreate", // 10
            "FolderDelete", // 11
            "FolderUpdate", // 12
            "MoveItems", // 13
            "GetItemEstimate", // 14
            "MeetingResponse", // 15
            "Search", // 16
            "Settings", // 17
            "Ping", // 18
            "ItemOperations", // 19
            "Provision", // 20
            "ResolveRecipients", // 21
            "ValidateCert" // 22
    };

    public static String getCmd(int value) {
        return commands[value];
    }

}
