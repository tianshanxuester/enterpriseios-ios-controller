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
 * Time: 15:02:32
 * To change this template use File | Settings | File Templates.
 */
public enum Base64ParameterCodes {

    AttachmentName,//0
    CollectionId,//1
    CollectionName,//2
    ItemId,//3
    LongId,//4
    ParentId,//5
    Occurrence,//6
    Options,//7
    User;//8

    public static Base64ParameterCodes getParam(int value) {
        switch (value) {
            case 0:
                return AttachmentName;
            case 1:
                return CollectionId;
            case 2:
                return CollectionName;
            case 3:
                return ItemId;
            case 4:
                return LongId;
            case 5:
                return ParentId;
            case 6:
                return Occurrence;
            case 7:
                return Options;
            case 9:
                return User;
            default:
                return null;
        }
    }

}
