param(
    [Parameter(Mandatory = $true)]
    [string]$ExePath
)

$resolvedExe = (Resolve-Path -LiteralPath $ExePath).Path
$exeItem = Get-Item -LiteralPath $resolvedExe
$exeItem.IsReadOnly = $false

Add-Type -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;

public static class PawLogManifestEditor
{
    private const uint LOAD_LIBRARY_AS_DATAFILE = 0x00000002;
    private static readonly IntPtr RT_MANIFEST = new IntPtr(24);
    private static readonly IntPtr MANIFEST_ID = new IntPtr(1);

    private delegate bool EnumResLangProc(IntPtr module, IntPtr type, IntPtr name, ushort language, IntPtr parameter);

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    private static extern IntPtr LoadLibraryEx(string fileName, IntPtr file, uint flags);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern bool FreeLibrary(IntPtr module);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern bool EnumResourceLanguages(IntPtr module, IntPtr type, IntPtr name, EnumResLangProc callback, IntPtr parameter);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern IntPtr FindResourceEx(IntPtr module, IntPtr type, IntPtr name, ushort language);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern IntPtr LoadResource(IntPtr module, IntPtr resource);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern IntPtr LockResource(IntPtr resourceData);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern uint SizeofResource(IntPtr module, IntPtr resource);

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    private static extern IntPtr BeginUpdateResource(string fileName, bool deleteExistingResources);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern bool UpdateResource(IntPtr update, IntPtr type, IntPtr name, ushort language, byte[] data, uint size);

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern bool EndUpdateResource(IntPtr update, bool discard);

    public static string Apply(string exePath)
    {
        IntPtr module = LoadLibraryEx(exePath, IntPtr.Zero, LOAD_LIBRARY_AS_DATAFILE);
        if (module == IntPtr.Zero) throw new Win32Exception(Marshal.GetLastWin32Error());

        var languages = new List<ushort>();
        var manifests = new Dictionary<ushort, byte[]>();

        try
        {
            EnumResLangProc callback = (h, t, n, language, p) =>
            {
                languages.Add(language);
                return true;
            };

            if (!EnumResourceLanguages(module, RT_MANIFEST, MANIFEST_ID, callback, IntPtr.Zero))
                throw new Win32Exception(Marshal.GetLastWin32Error(), "Manifest resource was not found.");

            foreach (ushort language in languages)
            {
                IntPtr resource = FindResourceEx(module, RT_MANIFEST, MANIFEST_ID, language);
                uint size = SizeofResource(module, resource);
                IntPtr loaded = LoadResource(module, resource);
                IntPtr data = LockResource(loaded);
                byte[] bytes = new byte[size];
                Marshal.Copy(data, bytes, 0, (int)size);

                string xml = Encoding.UTF8.GetString(bytes).TrimStart('\uFEFF', '\0');
                string updated = Regex.Replace(
                    xml,
                    "(<requestedExecutionLevel\\s+[^>]*level=\\\")[^\\\"]+(\\\")",
                    "$1requireAdministrator$2",
                    RegexOptions.IgnoreCase
                );

                if (updated == xml || !updated.Contains("requireAdministrator"))
                    throw new InvalidOperationException("requestedExecutionLevel could not be updated.");

                manifests[language] = new UTF8Encoding(false).GetBytes(updated);
            }
        }
        finally
        {
            FreeLibrary(module);
        }

        IntPtr updateHandle = BeginUpdateResource(exePath, false);
        if (updateHandle == IntPtr.Zero) throw new Win32Exception(Marshal.GetLastWin32Error());

        bool committed = false;
        try
        {
            foreach (var entry in manifests)
            {
                if (!UpdateResource(updateHandle, RT_MANIFEST, MANIFEST_ID, entry.Key, entry.Value, (uint)entry.Value.Length))
                    throw new Win32Exception(Marshal.GetLastWin32Error());
            }

            if (!EndUpdateResource(updateHandle, false))
                throw new Win32Exception(Marshal.GetLastWin32Error());

            committed = true;
        }
        finally
        {
            if (!committed) EndUpdateResource(updateHandle, true);
        }

        return "requireAdministrator embedded for language(s): " + string.Join(", ", languages);
    }
}
'@

[PawLogManifestEditor]::Apply($resolvedExe)
