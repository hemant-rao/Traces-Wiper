package com.example.data

/**
 * Centralized registry of all user-facing descriptions, warnings, tips, and guides.
 * This makes it extremely simple to edit, update, or localize all application text
 * in one single file without searching through individual compose files.
 */
object AppTexts {

    // --- APP WIDE METADATA ---
    const val APP_DISPLAY_NAME = "Traces Wiper"
    const val APP_FULL_NAME_SEO = "Traces Wiper - Secure File Shredder"

    // --- SECURE SHREDDER SUB-TAB ---
    const val SHRED_CONFIRM_TITLE = "PERMANENTLY DELETE FILES"
    const val SHRED_CONFIRM_MESSAGE = "You are about to permanently delete the selected files. Our advanced logic and deleting process will automatically erase all underlying traces, making recovery helper apps absolutely useless. This process is completely irreversible.\n\nAre you sure you want to proceed?"

    const val SHRED_WARNING_WARN = "WARNING: Secure Shredding completely neutralizes the file's underlying trace patterns, so it can't be brought back by normal recovery tools. This cannot be undone — use with absolute caution."
    const val SHRED_DESCRIPTION_NOTE = "Our smart logic and specialized deleting process automatically target and eliminate all file traces, making them definitively unrecoverable by standard tools. For complete flash sanitization of unallocated residues, also use Deep Wipe or a factory reset."

    // --- TEXT / CLIPBOARD WIPER SUB-TAB ---
    const val TEXT_WIPER_EXPLANATION = "Sensitive text you copy often stays on your phone's clipboard until something else replaces it. Paste it here and this tool removes it from the app and clears your clipboard, so it isn't left waiting for the next app to read.\n\nGood things to clear here:\n• Passwords or secret PIN codes\n• Bank account numbers or card details\n• One-time codes (OTP) and private snippets you just copied"
    const val TEXT_WIPER_PLACEHOLDER = "Paste sensitive text you just copied (e.g. passwords, bank details, OTP codes) to clear it from the app and your clipboard..."

    // --- DEEP STORAGE WIPE COMPONENT ---
    const val DEEP_WIPE_SUBTITLE = "Cleans the empty space on your phone, making sure older deleted files are definitively erased and cannot be recovered."
    const val DEEP_WIPE_HOW_IT_WORKS_TITLE = "HOW IT WORKS"
    const val DEEP_WIPE_GUIDELINE_1 = "When you normally delete files, parts of their data can stay hidden in the empty spaces of your phone's memory."
    const val DEEP_WIPE_GUIDELINE_2 = "This cleaner utilizes our specialized deleting process to thoroughly neutralize any leftover traces."
    const val DEEP_WIPE_GUIDELINE_3 = "Running this occasionally keeps your phone's storage clean, secure, and running smoothly."
    
    const val DEEP_WIPE_CHARGE_HEADER = "KEEP YOUR PHONE CHARGED"
    const val DEEP_WIPE_CHARGE_WARNING = "This process is thorough and can take several minutes to completely clean the empty space. We recommend keeping your phone plugged in while it runs."

    // --- DELETED TRACES / PERSISTENT REMNANTS COMPONENT ---
    const val TRACE_WIPER_SUBTITLE = "We find hidden fragments, cached thumbnails, and leftover traces of deleted files that still exist on your phone. Select items to permanently erase them."
    const val TRACE_WIPER_HOW_TITLE = "HOW REMNANTS CAN STILL BE RECOVERED"
    const val TRACE_WIPER_HOW_SUBTEXT = "When normal files are deleted, Android often leaves behind cached preview thumbnails or small index entries. Wiping these fragments completely neutralizes the leftover sectors, ensuring they can't be recovered or previewed."

    // --- RECOVER SENSITIVE DATA COMPONENT ---
    const val DATA_RECOVERY_SUBTITLE = "Scans folders, cache areas, recycled space, and hidden folders to find and restore deleted files that still exist on your storage."
    const val DATA_RECOVERY_HOW_TITLE = "HOW DATA RECOVERY WORKS"
    const val DATA_RECOVERY_HOW_SUBTEXT = "We scan for deleted file remains, hidden thumbnails, and cache elements that haven't been completely erased yet. Recovered files will be saved in your Downloads folder under \"Recovered_Traces\"."

    // --- SECURE ALGORITHMS & HELP GUIDE TAB ---
    const val GUIDE_Q_HOW_IT_WORKS = "How does it work?"
    const val GUIDE_A_HOW_IT_WORKS = "When you delete a file normally, only its entry is removed and the data stays on disk — that's why recovery apps can bring it back. We safely neutralize the file's structural sectors using our advanced deletion process, making sure normal recovery tools find absolutely nothing."

    const val GUIDE_Q_IS_IT_SAFE = "Is it safe?"
    const val GUIDE_A_IS_IT_SAFE = "Yes — the file is securely deleted and removed from your phone. It cannot be undone, so double-check before you shred."

    const val GUIDE_Q_CAN_FILES_RECOVER = "Can my files still be recovered?"
    const val GUIDE_A_CAN_FILES_RECOVER = "Not by ordinary recovery apps or services. Be aware that phone flash storage spreads writes across cells (wear-leveling), so no app can mathematically ensure 100% destruction. For the highest security confidence, also run Deep Wipe or do a factory reset."

    // --- GLOBAL BUTTONS, ALERTS & UI PILLS ---
    const val APP_TAGLINE = "100% OFFLINE • USER PRIVACY SECURED"
    const val SYSTEM_BUSY_TOAST = "🔒 System Busy: Secure operation is currently running."
    const val SYSTEM_BUSY_ACTIVE = "SECURE CLEANING ACTIVE"
    const val METRICS_TITLE = "HISTORICAL STORAGE STATISTICS"
    const val ABORT_ACTION = "CANCEL"
    const val START_OVERWRITE = "START SECURE WIPE"
    const val HOLD_TO_SHRED_PROGRESS = "HOLD TO DELETE: "
    const val HOLD_TO_SHRED_PROMPT = "HOLD TO PERMANENTLY ERASE"
    const val SELECT_FILES_PROMPT = "LOAD FILES TO DELETE"
}
