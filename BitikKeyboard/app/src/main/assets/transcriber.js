class TranscriptionEntry {
    constructor(symbol, latinTranscription, cyrillicTranscription, type, vowel = null, vowelPosition = 0, forceAddPreviousVowel = false) {
        this.symbol = symbol;
        this.latinTranscription = latinTranscription;
        this.cyrillicTranscription = cyrillicTranscription;
        this.type = type;
        this.vowel = vowel;
        this.vowelPosition = vowelPosition;
        this.forceAddPreviousVowel = forceAddPreviousVowel;
    }
}

class TranscriptionArgument {
    constructor() {
        this.transcriptionEntry = new TranscriptionEntry("", "", "", -1);
        this.vowelBefore = "";
        this.vowel = "";
        this.final = "";
        this.arguments = [];
    }

    get display() {
        return this.final || "null";
    }
}

class CorrentText {
    constructor() {
        this.transcriptionList = [
            new TranscriptionEntry("", "", "", -1, ""),

            // First Row
            new TranscriptionEntry("𐰶", "QI", "ҚЫ", 0, "I", 1),
            new TranscriptionEntry("𐰬", "aÑ", "аҢ", 1),
            new TranscriptionEntry("𐰧", "aÑ", "аҢ", 1),
            new TranscriptionEntry("𐰁", "A", "А", 4),
            new TranscriptionEntry("𐰀", "A", "А", 4),
            new TranscriptionEntry("𐰺", "aR", "аР", 1),
            new TranscriptionEntry("𐱄", "aT", "аТ", 1),
            new TranscriptionEntry("𐱃", "aT", "аТ", 1),
            new TranscriptionEntry("𐱇", "OT", "ОТ", 0, "O"),
            new TranscriptionEntry("𐰖", "aY", "аЙ", 1),
            new TranscriptionEntry("𐰗", "AY", "АЙ", 0, "A", 1),
            new TranscriptionEntry("𐰩", "U", "У", 4),
            new TranscriptionEntry("𐰃", "I", "Ы", 4),
            new TranscriptionEntry("𐰆", "O", "О", 4),
            new TranscriptionEntry("𐰯", "P", "П", 3),
            new TranscriptionEntry("𐰊", "F", "Ф", 3),

            // First Row - Shift
            new TranscriptionEntry("𐰷", "IQ", "ЫҚ", 0, "I"),
            new TranscriptionEntry("𐰭", "eÑ", "еҢ", 2),
            new TranscriptionEntry("𐰮", "eÑ", "еҢ", 2),
            new TranscriptionEntry("𐰅", "E", "Е", 5),
            new TranscriptionEntry("𐰂", "E", "Е", 5),
            new TranscriptionEntry("𐰼", "eR", "еР", 2),
            new TranscriptionEntry("𐱅", "eT", "еТ", 2),
            new TranscriptionEntry("𐰘", "eY", "еЙ", 2),
            new TranscriptionEntry("𐰇", "Ü", "Ү", 5),
            new TranscriptionEntry("𐰄", "İ", "И", 5),
            new TranscriptionEntry("𐰈", "Ö", "Ө", 5),

            // Second Row
            new TranscriptionEntry("𐰹", "OQ", "ОҚ", 0, "O"),
            new TranscriptionEntry("𐱂", "aS", "аС", 1),
            new TranscriptionEntry("𐰽", "SU", "СУ", 0, "U"),
            new TranscriptionEntry("𐰑", "aD", "аД", 1),
            new TranscriptionEntry("𐰒", "aD", "аД", 1),
            new TranscriptionEntry("𐰦", "NT", "НТ", 0),
            new TranscriptionEntry("𐰍", "aǦ", "аҒ", 1),
            new TranscriptionEntry("𐰎", "aǦ", "аҒ", 1),
            new TranscriptionEntry("𐱈", "RT", "РТ", 0),
            new TranscriptionEntry("𐰳", "aJ", "аЖ", 1),
            new TranscriptionEntry("𐰴", "aQ", "аҚ", 1),
            new TranscriptionEntry("𐰛", "H", "Х", 3),
            new TranscriptionEntry("𐰞", "aL", "аЛ", 1),
            new TranscriptionEntry("𐰟", "aL", "аЛ", 1),

            // Second Row - Shift
            new TranscriptionEntry("𐰸", "UQ", "УҚ", 0, "U"),
            new TranscriptionEntry("𐰾", "eS", "еС", 2),
            new TranscriptionEntry("𐰓", "eD", "еД", 2),
            new TranscriptionEntry("𐰨", "NÇ", "НЧ", 0),
            new TranscriptionEntry("𐰏", "eG", "еГ", 2),
            new TranscriptionEntry("𐰐", "eG", "еГ", 2),
            new TranscriptionEntry("𐰡", "LT", "ЛТ", 0),
            new TranscriptionEntry("𐰙", "eJ", "еЖ", 2),
            new TranscriptionEntry("𐰚", "eK", "еК", 2),
            new TranscriptionEntry("𐰠", "eL", "еЛ", 2),

            // Third Row
            new TranscriptionEntry("𐰕", "Z", "З", 3),
            new TranscriptionEntry("𐱀", "aŞ", "аЩ", 1),
            new TranscriptionEntry("𐰲", "Ç", "Ч", 3),
            new TranscriptionEntry("𐰱", "ÇƗ", "ЧЫ", 0, "Ɨ"),
            new TranscriptionEntry("𐰜", "ÜK", "ҮК", 0),
            new TranscriptionEntry("𐰉", "aB", "аБ", 1),
            new TranscriptionEntry("𐰋", "V", "В", 3),
            new TranscriptionEntry("𐰣", "aN", "аН", 1),
            new TranscriptionEntry("𐰢", "M", "М", 3),

            // Third Row - Shift
            new TranscriptionEntry("𐰿", "eŞ", "еШ", 2),
            new TranscriptionEntry("𐰝", "ÖK", "ӨК", 0),
            new TranscriptionEntry("𐰌", "eB", "еБ", 2),
            new TranscriptionEntry("𐰥", "eN", "еН", 2)
        ];

        this.arguments = [];
    }

    getTranscription(text) {
        let textTemp = "";
        this.arguments = [];

        let newArgument = new TranscriptionArgument();
        newArgument.vowel = "";
        newArgument.transcriptionEntry = this.transcriptionList[0];
        this.arguments.push(newArgument);

        let index = 0;

        while (index < text.length) {
            const codePoint = text.codePointAt(index);
            const symbol = String.fromCodePoint(codePoint);
            const transcriptionData = this.transcriptionList.find(t => t.symbol === symbol);

            if (!transcriptionData) {
                index++;

                newArgument = new TranscriptionArgument();
                newArgument.vowel = "";
                newArgument.transcriptionEntry = this.transcriptionList[0];
                this.arguments.push(newArgument);

                textTemp += symbol;
                continue;
            }

            newArgument = new TranscriptionArgument();
            newArgument.transcriptionEntry = this.transcriptionList[0];

            const lastTranscriptionData = this.arguments[this.arguments.length - 1];
            newArgument.vowelBefore = lastTranscriptionData.vowel;
            newArgument.vowel = lastTranscriptionData.vowel;

            this.arguments.push(newArgument);

            if (!/\s/.test(symbol)) {
                newArgument.transcriptionEntry = transcriptionData;

                switch (transcriptionData.type) {
                    case 0: // Strong Character
                        this.processStrongCharacter(newArgument, lastTranscriptionData);
                        break;
                    case 1: // Hard Consonant
                    case 2: // Soft Consonant
                    case 3: // Strong Consonant
                        this.processHardConsonant(newArgument, lastTranscriptionData);
                        break;
                    case 4: // Strong Vowel
                    case 5: // Flexible Vowel
                        newArgument.final = transcriptionData.latinTranscription;
                        newArgument.vowel = transcriptionData.latinTranscription;
                        newArgument.vowelBefore = lastTranscriptionData.vowel;
                        break;
                }
            }

            index += symbol.length;
            textTemp += newArgument.final;
        }

        // Prepare arguments for C# to display
        const exportArgs = this.arguments.map(arg => ({
            transcriptionEntry: arg.transcriptionEntry,
            vowelBefore: arg.vowelBefore,
            vowel: arg.vowel,
            final: arg.final,
            arguments: arg.arguments
        }));

        // Send to C# for display in Unity inspector
        StoreArguments(exportArgs);

        return textTemp;
    }

    processStrongCharacter(transcriptionArgument, lastTranscriptionArgumen) {
        if (!transcriptionArgument.transcriptionEntry.vowel) {
            transcriptionArgument.vowel = transcriptionArgument.transcriptionEntry.vowel || "";
        } else {
            transcriptionArgument.vowel = transcriptionArgument.transcriptionEntry.vowel;
            transcriptionArgument.vowelBefore = transcriptionArgument.transcriptionEntry.vowel;
        }

        transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription;
        return transcriptionArgument.final;
    }

    processHardConsonant(transcriptionArgument, lastTranscriptionArgumen) {
        const isStrongConsonant = transcriptionArgument.transcriptionEntry.type === 3;
        let vowelSelf = transcriptionArgument.transcriptionEntry.type === 2 ? "e" : "a";

        if (isStrongConsonant && lastTranscriptionArgumen.vowel) {
            vowelSelf = lastTranscriptionArgumen.vowel.toLowerCase();
        }

        let replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(
            new RegExp(vowelSelf, "g"),
            ""
        );

        if (lastTranscriptionArgumen.transcriptionEntry.type === -1) {
            replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(vowelSelf, "");
            transcriptionArgument.final = replacedWithEmpty_Self;
            transcriptionArgument.vowel = vowelSelf;
            transcriptionArgument.arguments.push("Made first letter");
        } else {
            if (lastTranscriptionArgumen.transcriptionEntry.type === 2 ||
                lastTranscriptionArgumen.transcriptionEntry.type === 3) {
                replacedWithEmpty_Self = transcriptionArgument.transcriptionEntry.latinTranscription.replace(vowelSelf, "");
            }

            let replacedWithLowerCase_LastVowel = transcriptionArgument.transcriptionEntry.latinTranscription
                .replace(new RegExp(vowelSelf, "g"), lastTranscriptionArgumen.vowel.toLowerCase());

            if (transcriptionArgument.transcriptionEntry.type === 3 && lastTranscriptionArgumen.vowel) {
                replacedWithLowerCase_LastVowel = vowelSelf + transcriptionArgument.transcriptionEntry.latinTranscription;
            }

            switch (lastTranscriptionArgumen.transcriptionEntry.type) {
                case 0: // Strong Character
                    if (lastTranscriptionArgumen.vowel) {
                        transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription
                            .replace(new RegExp(vowelSelf, "g"), lastTranscriptionArgumen.vowel.toLowerCase());
                        transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                        transcriptionArgument.arguments.push("Following last Strong Character");
                    } else {
                        transcriptionArgument.final = replacedWithEmpty_Self;
                        transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                        transcriptionArgument.vowelBefore = lastTranscriptionArgumen.vowel;
                        transcriptionArgument.arguments.push("Following last Strong Character with Vowel");
                    }
                    break;
                case 1: // Hard Consonant
                    transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.arguments.push("Following last hard consonant");
                    break;
                case 2: // Soft Consonant
                    transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.arguments.push("Following last soft consonant");
                    break;
                case 3: // Strong Consonant
                    if (!lastTranscriptionArgumen.vowel) {
                        transcriptionArgument.final = transcriptionArgument.transcriptionEntry.latinTranscription;
                    } else {
                        lastTranscriptionArgumen.vowel = vowelSelf;
                        transcriptionArgument.final = replacedWithLowerCase_LastVowel;
                    }
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    transcriptionArgument.arguments.push("Following last strong consonant");
                    break;
                case 4: // Strong Vowel
                case 5: // Flexible Vowel
                    if (vowelSelf === lastTranscriptionArgumen.vowel.toLowerCase()) {
                        if (lastTranscriptionArgumen.vowel.toLowerCase() !== lastTranscriptionArgumen.vowelBefore.toLowerCase()) {
                            transcriptionArgument.final = replacedWithEmpty_Self;
                            transcriptionArgument.arguments.push("Same vowel found, so double vowel");
                        } else {
                            transcriptionArgument.final = `${lastTranscriptionArgumen.vowel}${replacedWithEmpty_Self}`;
                            transcriptionArgument.arguments.push("Vowel change");
                        }
                    } else {
                        transcriptionArgument.final = replacedWithEmpty_Self;
                        transcriptionArgument.arguments.push("Its vowel is different from standard vowel");
                    }
                    transcriptionArgument.vowel = lastTranscriptionArgumen.vowel;
                    break;
            }
        }

        return transcriptionArgument.final;
    }
}

// Make available for Unity
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CorrentText;
}