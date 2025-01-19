import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible"

// Menu items.
const items = [
  {
    title: "Home",
    url: "#",
  },
  {
    title: "Inbox",
    url: "#",
  },
  {
    title: "Calendar",
    url: "#",
  },
  {
    title: "Search",
    url: "#",
  },
  {
    title: "Settings",
    url: "#",
  },
]

export function SpellSidebar() {
  return (
    <Sidebar class="left-auto">
      <SidebarContent>
        <Collapsible >
          <CollapsibleTrigger class="bg-zinc-800 p-1 text-sm w-full text-left	">Spells</CollapsibleTrigger>
          <CollapsibleContent>
            Yes. Free to use for personal and commercial projects. No attribution
            required.
          </CollapsibleContent>

        </Collapsible>

        <Collapsible >
          <CollapsibleTrigger class="bg-zinc-800 p-1 text-sm w-full text-left	">Macros</CollapsibleTrigger>
          <CollapsibleContent>
            Yes. Free to use for personal and commercial projects. No attribution
            required.
          </CollapsibleContent>

        </Collapsible>
      </SidebarContent>
    </Sidebar>
  )
}
